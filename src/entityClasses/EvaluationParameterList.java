package entityClasses;

import java.util.ArrayList;

import database.Database;

/*******
 * <p> Title: EvaluationParameterList Class </p>
 *
 * <p> Description: A model class that manages a collection of
 * EvaluationParameter objects loaded from the H2 database. It acts as an
 * in memory cache of the evaluation parameters owned by a particular staff
 * user. </p>
 *
 * @author James Suchovic (Team 3)
 *
 * @version 1.00 2026-07-26 Initial implementation
 */
public class EvaluationParameterList {

	/** Evaluation parameters currently loaded from the database. */
	private ArrayList<EvaluationParameter> parameterList;

	/** Staff user whose parameters are currently loaded. */
	private String staffUsername;

	/** Shared reference to the application database. */
	private static Database theDatabase =
		applicationMain.FoundationsMain.database;

	/*******
	 * <p> Method: EvaluationParameterList() Constructor </p>
	 *
	 * <p> Description: Creates an EvaluationParameterList and loads all
	 * parameters owned by the supplied staff user. </p>
	 *
	 * @param inStaffUsername the staff user whose parameters should be loaded
	 */
	public EvaluationParameterList(String inStaffUsername) {

		staffUsername = inStaffUsername;

		parameterList =
			theDatabase.getEvaluationParametersForStaff(
				staffUsername
			);
	}

	/*******
	 * <p> Method: refreshList() </p>
	 *
	 * <p> Description: Reloads the current staff user's evaluation parameters
	 * from the database. </p>
	 */
	public void refreshList() {

		parameterList =
			theDatabase.getEvaluationParametersForStaff(
				staffUsername
			);
	}

	/*******
	 * <p> Method: getEvaluationParameter() </p>
	 *
	 * <p> Description: Searches the in memory parameter list for the
	 * parameter with the specified ID. </p>
	 *
	 * @param parameterID the parameter ID to locate
	 * @return the matching parameter, or null if none exists
	 */
	public EvaluationParameter getEvaluationParameter(
			int parameterID) {

		EvaluationParameter rParameter = null;
		boolean found = false;
		int i = 0;

		while (!found && i != parameterList.size()) {

			if (parameterList.get(i).getParameterID()
					== parameterID) {

				rParameter = parameterList.get(i);
				found = true;
			}

			i++;
		}

		return rParameter;
	}

	/*******
	 * <p> Method: getParameterByMetric() </p>
	 *
	 * <p> Description: Searches for a parameter using its metric and optional
	 * thread scope. This supports enforcing one parameter per metric and
	 * scope for each staff user. </p>
	 *
	 * @param metric the metric to locate
	 * @param threadID the thread scope, or null for all threads
	 * @return the matching parameter, or null if none exists
	 */
	public EvaluationParameter getParameterByMetric(
			String metric,
			Integer threadID) {

		for (EvaluationParameter parameter : parameterList) {

			boolean sameMetric =
				parameter.getMetric().equals(metric);

			boolean sameThread =
				parameter.getThreadID() == null
					? threadID == null
					: parameter.getThreadID().equals(threadID);

			if (sameMetric && sameThread) {
				return parameter;
			}
		}

		return null;
	}

	/*******
	 * <p> Method: getEvaluationParameterList() </p>
	 *
	 * <p> Description: Returns the complete in memory collection of
	 * evaluation parameters for the current staff user. </p>
	 *
	 * @return the current evaluation parameter list
	 */
	public ArrayList<EvaluationParameter>
			getEvaluationParameterList() {

		return parameterList;
	}

	/*******
	 * <p> Method: deleteEvaluationParameter() </p>
	 *
	 * <p> Description: Deletes the parameter with the supplied ID through
	 * the database layer. </p>
	 *
	 * @param parameterID the parameter ID to delete
	 */
	public void deleteEvaluationParameter(
			int parameterID) {

		theDatabase.deleteEvaluationParameter(
			parameterID,
			staffUsername
		);
	}
}