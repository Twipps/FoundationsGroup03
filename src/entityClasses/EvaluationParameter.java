package entityClasses;

/*******
 * <p> Title: EvaluationParameter Class </p>
 *
 * <p> Description: This class stores one configurable participation
 * evaluation parameter created by a staff user. Evaluation parameters are
 * used to compare a student's current participation statistics against
 * staff defined requirements. </p>
 *
 * <p> Examples include minimum post count, minimum reply count, and minimum
 * number of discussion threads participated in. The result of an evaluation
 * is calculated at runtime and is not stored in this object. </p>
 *
 * @author James Suchovic (Team 3)
 *
 * @version 1.00 2026-07-26 Initial implementation
 */
public class EvaluationParameter {

	/** Unique database identifier for this evaluation parameter. */
	private int parameterID;

	/** Username of the staff member who owns this parameter. */
	private String staffUsername;

	/** Human-readable name displayed in the GUI. */
	private String name;

	/**
	 * Statistic evaluated by this parameter.
	 * Examples: POST_COUNT, REPLY_COUNT, THREAD_COUNT.
	 */
	private String metric;

	/**
	 * Comparison operation applied during evaluation.
	 * Example: GREATER_THAN_OR_EQUAL.
	 */
	private String comparisonOperator;

	/** Required numeric value for the selected metric. */
	private int threshold;

	/** Optional explanation of the parameter. */
	private String description;

	/**
	 * Optional thread scope.
	 * Null indicates that the parameter applies across all threads.
	 */
	private Integer threadID;

	/** Determines whether this parameter is included in evaluations. */
	private boolean active;

	/*******
	 * <p> Method: EvaluationParameter() New Parameter Constructor </p>
	 *
	 * <p> Description: Creates a new evaluation parameter before it is
	 * inserted into the database. The parameterID is initialized to -1 because
	 * the database has not assigned an ID yet. </p>
	 *
	 * @param inStaffUsername the staff member who owns the parameter
	 * @param inName the display name of the parameter
	 * @param inMetric the statistic being evaluated
	 * @param inComparisonOperator the comparison operation
	 * @param inThreshold the required numeric value
	 * @param inDescription an optional description
	 * @param inThreadID an optional thread scope, or null for all threads
	 */
	public EvaluationParameter(
			String inStaffUsername,
			String inName,
			String inMetric,
			String inComparisonOperator,
			int inThreshold,
			String inDescription,
			Integer inThreadID) {

		parameterID = -1;
		staffUsername = inStaffUsername;
		name = inName;
		metric = inMetric;
		comparisonOperator = inComparisonOperator;
		threshold = inThreshold;
		description = inDescription;
		threadID = inThreadID;
		active = true;
	}

	/*******
	 * <p> Method: EvaluationParameter() Database Reconstruction
	 * Constructor </p>
	 *
	 * <p> Description: Reconstructs an existing evaluation parameter from a
	 * database record. </p>
	 *
	 * @param inParameterID the database identifier
	 * @param inStaffUsername the owning staff username
	 * @param inName the display name
	 * @param inMetric the statistic being evaluated
	 * @param inComparisonOperator the comparison operation
	 * @param inThreshold the required numeric value
	 * @param inDescription the parameter description
	 * @param inThreadID the optional thread scope
	 * @param inActive whether the parameter is active
	 */
	public EvaluationParameter(
			int inParameterID,
			String inStaffUsername,
			String inName,
			String inMetric,
			String inComparisonOperator,
			int inThreshold,
			String inDescription,
			Integer inThreadID,
			boolean inActive) {

		parameterID = inParameterID;
		staffUsername = inStaffUsername;
		name = inName;
		metric = inMetric;
		comparisonOperator = inComparisonOperator;
		threshold = inThreshold;
		description = inDescription;
		threadID = inThreadID;
		active = inActive;
	}

	/**
	 * Updates the display name of this parameter.
	 *
	 * @param inName the new display name
	 */
	public void updateName(String inName) {
		name = inName;
	}

	/**
	 * Updates the metric evaluated by this parameter.
	 *
	 * @param inMetric the new metric
	 */
	public void updateMetric(String inMetric) {
		metric = inMetric;
	}

	/**
	 * Updates the comparison operation.
	 *
	 * @param inComparisonOperator the new comparison operation
	 */
	public void updateComparisonOperator(
			String inComparisonOperator) {

		comparisonOperator = inComparisonOperator;
	}

	/**
	 * Updates the required threshold.
	 *
	 * @param inThreshold the new threshold
	 */
	public void updateThreshold(int inThreshold) {
		threshold = inThreshold;
	}

	/**
	 * Updates the parameter description.
	 *
	 * @param inDescription the new description
	 */
	public void updateDescription(String inDescription) {
		description = inDescription;
	}

	/**
	 * Updates the optional thread scope.
	 *
	 * @param inThreadID the new thread ID, or null for all threads
	 */
	public void updateThreadID(Integer inThreadID) {
		threadID = inThreadID;
	}

	/**
	 * Updates whether the parameter is active.
	 *
	 * @param inActive the new active status
	 */
	public void updateActive(boolean inActive) {
		active = inActive;
	}

	public int getParameterID() {
		return parameterID;
	}

	public String getStaffUsername() {
		return staffUsername;
	}

	public String getName() {
		return name;
	}

	public String getMetric() {
		return metric;
	}

	public String getComparisonOperator() {
		return comparisonOperator;
	}

	public int getThreshold() {
		return threshold;
	}

	public String getDescription() {
		return description;
	}

	public Integer getThreadID() {
		return threadID;
	}

	public boolean isActive() {
		return active;
	}
}