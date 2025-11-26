/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package c4uburntester;

/**
 *
 * @author justinguedes
 */
public class Stats
{
	private static long successful = 0;
	private static long failure = 0;

	public static void success()
	{
		successful++;
	}

	public static long successful()
	{
		return successful;
	}

	public static void failed()
	{
		failure++;
	}

	public static long failures()
	{
		return failure;
	}
}
