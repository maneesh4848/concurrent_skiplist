package concurrent_skiplist;
import java.util.Random;
import java.util.concurrent.locks.Lock;

public final class skiplist
{
	private static final int maxheight = 25;
	final node head = new node(Integer.MIN_VALUE, maxheight);
	final node tail = new node(Integer.MAX_VALUE, maxheight);

	//Constructor
	public skiplist()
	{
		for(int i=0;i<head.next.length;i++)
		{
			head.next[i] = tail;
		}
	}

	private int randomheight()
	{
		Random rand = new Random();
		int height = 0;
		while(rand.nextInt(2) == 1 && height < maxheight)
		{
			height++;
		}
		return height;
	}

	public int find(int data, node[] pred, node[] succ)
	{
		int found = -1;
		node prednode = head;
		for(int i = maxheight; i >= 0; i--)
		{
			node currnode = prednode.next[i];
			while(currnode.data < data)
			{
				prednode = currnode;
				currnode = currnode.next[i];
			}
			
			//Reached the node for the first time
			if(data == currnode.data && found == -1)
			{
				found = i;
			}
			//Recording predecessors and successors
			pred[i] = prednode;
			succ[i] = currnode;
		}
		//Returning the highest level at which the node was found
		return found;
	}

	public boolean contains(int data)
	{
		node[] preds = new node[maxheight+1];
		node[] succs = new node[maxheight+1];
		int level = find(data,preds,succs);
		
		//Checking if the node is present and inserted
		//and not marked for deletion
		if(level >= 0 && succs[level].fulllink && !succs[level].mark)
		{
			return true;
		}
		return false;
	}

	public void print()
	{
		for(int level = maxheight; level >= 0; level--)
		{
			node curr = head.next[level];
			while(curr != tail)
			{
				if(curr.fulllink && !curr.mark)
				{
					System.out.print(Integer.toString(curr.data) + ' ');
				}
				curr = curr.next[level];
			}
			System.out.println();
		}
	}
	
	public boolean add(int data)
	{
		//highestlevel: highest level until which the node is inserted
		int highestlevel = randomheight();
		node[] preds = new node[maxheight+1];
		node[] succs = new node[maxheight+1];
		while(true)
		{
			int level = find(data,preds,succs);
			
			//level >= 0 -> node is found in skip list already
			if(level >= 0)
			{
				node datanode = succs[level];
				if(!datanode.mark)
				{
					//waiting for insertion by other thread
					while(!datanode.fulllink)
					{}
					return false;
				}
				//Node is marked for deletion, 
				//so restart the process and insert the node after its deletion
				continue;
			}
			
			int highestlocked = -1;
			try
			{
				node pred,succ;
				//valid field tells if the insertion is still feasible
				//i.e., if the predecessor and successor are still in the skip list 
				//and no node is being inserted between them
				boolean valid = true;
				for(int i = 0; i <= highestlevel && valid; i++)
				{
					pred = preds[i];
					succ = succs[i];
					pred.lock();
					highestlocked = i;
					valid = !pred.mark && !succ.mark && pred.next[i] == succ;
				}
				
				// if valid is false, then either the predecessor or the successor is 
				//either deleted or being deleted
				if(!valid)
				{
					continue;
				}
				
				//Insertion of new node begins
				node newnode = new node(data,highestlevel);
				for(int i = 0; i <= highestlevel; i++)
				{
					preds[i].next[i] = newnode;
					newnode.next[i] = succs[i];
				}
				//Node is fully linked at all levels
				newnode.fulllink = true;
				return true;
			}
			finally
			{
				//Unlocking predecessors
				for(int i = 0; i <= highestlocked; i++)
				{
					preds[i].unlock();
				}
			}
		}
	}
	
	public boolean remove(int data)
	{
		node deletenode = null;
		int highestlevel = -1;
		boolean marked = false;
		node[] preds = new node[maxheight+1];
		node[] succs = new node[maxheight+1];
		while(true)
		{
			int level = find(data,preds,succs);
			if(level >= 0)
			{
				//deletenode is the highest node 
				//with the corresponding data value in the skip list
				deletenode = succs[level];
			}
			
			//Checking if deletenode is inserted completely
			//and not already being deleted by another thread
			if(marked | (level >= 0 && !deletenode.mark && deletenode.fulllink && deletenode.toplevel == level))
			{
				//Not yet marked
				if(!marked)
				{
					//Logically marking the node
					highestlevel = level;
					deletenode.lock();
					if(deletenode.mark)
					{
						deletenode.unlock();
						return false;
					}
					deletenode.mark = true;
					marked = true;
				}
				
				int highestlocked = -1;
				try
				{
					node pred;
					boolean valid = true;
					
					//Checking if the predecessors obtained are still valid
					for(int i = 0; i <= highestlevel && valid; i++)
					{
						pred = preds[i];
						pred.lock();
						highestlocked = i;
						valid = !pred.mark && pred.next[i] == deletenode;
					}
					
					//if valid is false, then the predecessor might have been marked
					//or modified, restarting the process in this case
					if(!valid)
					{
						continue;
					}
					
					//Deleting the node
					for(int i = highestlevel; i >= 0; i--)
					{
						preds[i].next[i] = deletenode.next[i];
					}
					deletenode.unlock();
					return true;
				}
				finally
				{
					//Unlocking predecessors
					for(int i = 0; i <= highestlocked; i++)
					{
						preds[i].unlock();
					}
				}
			}
			else
			{
				return false;
			}
		}
	}

}
