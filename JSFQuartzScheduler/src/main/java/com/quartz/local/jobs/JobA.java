/*
 * 
 */
package com.quartz.local.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * The Class JobA.
 */
public class JobA implements Job {

	/*
	 * (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	@Override
	public void execute(final JobExecutionContext context)
			throws JobExecutionException {
		System.out.println("Job A is runing");

	}

}
