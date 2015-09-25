/*
 *
 */
package com.quartz.local.scheduler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.ee.servlet.QuartzInitializerListener;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

/**
 * The Class SchedulerBean.
 */
@ManagedBean(name = "scheduler")
@SessionScoped
public class SchedulerBean implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The scheduler. */
	private final Scheduler scheduler;

	/** The quartz job list. */
	private final List<QuartzJob> quartzJobList = new ArrayList<QuartzJob>();

	/**
	 * Instantiates a new scheduler bean.
	 *
	 * @throws SchedulerException the scheduler exception
	 */
	public SchedulerBean() throws SchedulerException {

		final ServletContext servletContext = (ServletContext) FacesContext
				.getCurrentInstance().getExternalContext().getContext();
		final StdSchedulerFactory stdSchedulerFactory = (StdSchedulerFactory) servletContext
				.getAttribute(QuartzInitializerListener.QUARTZ_FACTORY_KEY);
		scheduler = stdSchedulerFactory.getScheduler();

		// loop jobs by group
		for (final String groupName : scheduler.getJobGroupNames()) {

			// get jobkey
			for (final JobKey jobKey : scheduler.getJobKeys(GroupMatcher
					.jobGroupEquals(groupName))) {

				final String jobName = jobKey.getName();
				final String jobGroup = jobKey.getGroup();

				// get job's trigger
				final List<Trigger> triggers = (List<Trigger>) scheduler
						.getTriggersOfJob(jobKey);
				final Date nextFireTime = triggers.get(0).getNextFireTime();

				quartzJobList
						.add(new QuartzJob(jobName, jobGroup, nextFireTime));

			}

		}
		scheduler.standby();
	}

	public String startScheduler() throws SchedulerException {
		if (scheduler.isInStandbyMode()) {
			scheduler.start();
			System.out.println("::::Scheduler Started::::");
		}
		return "";
	}

	public boolean isInStandbyMode() throws SchedulerException {
		return scheduler.isInStandbyMode();
	}

	public boolean isStarted() throws SchedulerException {
		return scheduler.isStarted();
	}

	public String standByScheduler() throws SchedulerException {
		if (scheduler.isStarted()) {
			scheduler.standby();
			System.out.println("::::Scheduler StandBy::::");
		}
		return "";
	}

	/**
	 * Fire now.
	 *
	 * @param jobName the job name
	 * @param jobGroup the job group
	 * @throws SchedulerException the scheduler exception
	 */
	// trigger a job
	public void fireNow(final String jobName, final String jobGroup)
			throws SchedulerException {
		final JobKey jobKey = new JobKey(jobName, jobGroup);
		scheduler.triggerJob(jobKey);
		getNextFireTime(jobName, jobGroup);
		System.out.println("Fired Job:: " + jobName);
	}

	/**
	 * Gets the next fire time.
	 *
	 * @param jobName the job name
	 * @param jobGroup the job group
	 * @return the next fire time
	 * @throws SchedulerException the scheduler exception
	 */
	public void getNextFireTime(final String jobName, final String jobGroup) throws SchedulerException {
		for (final QuartzJob job : quartzJobList) {
			if (jobName.compareToIgnoreCase(job.getJobName()) == 0) {
				final Trigger trigger = scheduler.getTriggersOfJob(new JobKey(jobName, jobGroup)).get(0);
				job.setNextFireTime(trigger.getNextFireTime());
			}
		}
	}

	/**
	 * Pause now.
	 *
	 * @param jobName the job name
	 * @param jobGroup the job group
	 * @throws SchedulerException the scheduler exception
	 */
	public void pauseNow(final String jobName, final String jobGroup)
			throws SchedulerException {
		final JobKey jobKey = new JobKey(jobName, jobGroup);
		scheduler.pauseJob(jobKey);
		System.out.println("Paused Job:: " + jobName);
	}

	/**
	 * Resume now.
	 *
	 * @param jobName the job name
	 * @param jobGroup the job group
	 * @throws SchedulerException the scheduler exception
	 */
	public void resumeNow(final String jobName, final String jobGroup)
			throws SchedulerException {
		final JobKey jobKey = new JobKey(jobName, jobGroup);
		scheduler.resumeJob(jobKey);
		getNextFireTime(jobName, jobGroup);
		System.out.println("Resumed Job:: " + jobName);
	}

	/**
	 * Trigger status.
	 *
	 * @param jobName the job name
	 * @param jobGroup the job group
	 * @return the trigger state
	 * @throws SchedulerException the scheduler exception
	 */
	private TriggerState triggerStatus(final String jobName, final String jobGroup)
			throws SchedulerException {
		final JobKey jobKey = new JobKey(jobName, jobGroup);
		// get job's trigger
		final List<Trigger> triggers = (List<Trigger>) scheduler
				.getTriggersOfJob(jobKey);
		final TriggerState triggerState = scheduler.getTriggerState(triggers.get(0).getKey());
		return triggerState;
	}

	/**
	 * Checks if is trigger paused.
	 *
	 * @param jobName the job name
	 * @param jobGroup the job group
	 * @return true, if is trigger paused
	 * @throws SchedulerException the scheduler exception
	 */
	public boolean isTriggerPaused(final String jobName, final String jobGroup) throws SchedulerException {
		final TriggerState triggerState = triggerStatus(jobName, jobGroup);
		if (triggerState.compareTo(TriggerState.PAUSED) == 0) {
			return true;
		}
		return false;
	}

	/**
	 * Gets the quartz job list.
	 *
	 * @return the quartz job list
	 */
	public List<QuartzJob> getQuartzJobList() {
		return quartzJobList;
	}

	/**
	 * The Class QuartzJob.
	 */
	public static class QuartzJob implements Serializable {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = -1488669777737811715L;

		/** The job name. */
		String jobName;

		/** The job group. */
		String jobGroup;

		/** The next fire time. */
		Date nextFireTime;

		/**
		 * Instantiates a new quartz job.
		 *
		 * @param jobName the job name
		 * @param jobGroup the job group
		 * @param nextFireTime the next fire time
		 */
		public QuartzJob(final String jobName, final String jobGroup, final Date nextFireTime) {

			this.jobName = jobName;
			this.jobGroup = jobGroup;
			this.nextFireTime = nextFireTime;
		}

		/**
		 * Gets the job name.
		 *
		 * @return the job name
		 */
		public String getJobName() {
			return jobName;
		}

		/**
		 * Sets the job name.
		 *
		 * @param jobName the new job name
		 */
		public void setJobName(final String jobName) {
			this.jobName = jobName;
		}

		/**
		 * Gets the job group.
		 *
		 * @return the job group
		 */
		public String getJobGroup() {
			return jobGroup;
		}

		/**
		 * Sets the job group.
		 *
		 * @param jobGroup the new job group
		 */
		public void setJobGroup(final String jobGroup) {
			this.jobGroup = jobGroup;
		}

		/**
		 * Gets the next fire time.
		 *
		 * @return the next fire time
		 */
		public Date getNextFireTime() {
			return nextFireTime;
		}

		/**
		 * Sets the next fire time.
		 *
		 * @param nextFireTime the new next fire time
		 */
		public void setNextFireTime(final Date nextFireTime) {
			this.nextFireTime = nextFireTime;
		}

	}

}