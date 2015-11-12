package concurrent_skiplist;

import java.io.IOException;

public class test
{
	public static void main(String[] args)
	{
		run_small();
		run_big();
		System.exit(0);
	}
	
	public static void run_small()
	{
		long [][] small;
		small = new long[6][3];

		for(int i = 0; i < 5; i++)
		{
			for(int j = 0; j < 3; j++)
			{
				System.out.println(i);
				System.out.println(j);
				skiplisttest listtest = null;
				try
				{
					listtest = new skiplisttest(i,0);
				}
				catch (IOException e1)
				{
				}
				try
				{
					small[i][j] = listtest.testParallelBoth();
				}
				catch (Exception e)
				{
				}
			}
		}

		long avg[] = new long[6];
		for(int i = 0; i < 5; i++)
		{
			for(int j = 0; j < 3; j++)
			{
				System.out.println(Integer.toString(i) + " " + Integer.toString(j) + " " + Long.toString(small[i][j]));
				avg[i] += small[i][j];
			}
			avg[i] /= 3;
			System.out.println(Integer.toString(i) + " " + Long.toString(avg[i]));
		}
	}
	
	public static void run_big()
	{
		long [][] big;
		big = new long[6][3];
		for(int i = 0; i < 5; i++)
		{
			for(int j = 0; j < 1; j++)
			{
				skiplisttest listtest = null;
				try
				{
					listtest = new skiplisttest(i,1);
				}
				catch (IOException e1)
				{
				}
				try
				{
					big[i][j] = listtest.testParallelBoth();
				}
				catch (Exception e)
				{
				}
			}
		}
		
		long avg2[] = new long[6];
		for(int i = 0; i < 5; i++)
		{
			for(int j = 0; j < 1; j++)
			{
				System.out.println(Integer.toString(i) + " " + Integer.toString(j) + " " + Long.toString(big[i][j]));
				avg2[i] += big[i][j];
			}
			//avg2[i] /= 2;
			System.out.println(Integer.toString(i) + " " + Long.toString(avg2[i]));
		}
	}
}
