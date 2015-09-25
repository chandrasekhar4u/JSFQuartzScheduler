/*
 * 
 */
package com.quartz.local.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class JobC implements Job {

	@Override
	public void execute(final JobExecutionContext context)
			throws JobExecutionException {
		System.out.println("Job C is runing");

	}

}