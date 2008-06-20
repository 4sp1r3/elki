package de.lmu.ifi.dbs.preprocessing;

import de.lmu.ifi.dbs.data.DatabaseObject;
import de.lmu.ifi.dbs.database.AssociationID;
import de.lmu.ifi.dbs.database.Database;
import de.lmu.ifi.dbs.database.DatabaseEvent;
import de.lmu.ifi.dbs.database.DatabaseListener;
import de.lmu.ifi.dbs.utilities.UnableToComplyException;
import de.lmu.ifi.dbs.utilities.Util;
import de.lmu.ifi.dbs.utilities.optionhandling.AttributeSettings;
import de.lmu.ifi.dbs.utilities.optionhandling.ClassParameter;
import de.lmu.ifi.dbs.utilities.optionhandling.Flag;
import de.lmu.ifi.dbs.utilities.optionhandling.OptionHandler;
import de.lmu.ifi.dbs.utilities.optionhandling.ParameterException;
import de.lmu.ifi.dbs.utilities.optionhandling.WrongParameterValueException;

import java.util.List;

/**
 * Handler class for all distance function using a preprocessor.
 *
 * @author Elke Achtert 
 */
public class PreprocessorHandler<O extends DatabaseObject,P extends Preprocessor<O>> implements DatabaseListener {

  /**
   * Parameter for preprocessor.
   */
  public static final String PREPROCESSOR_CLASS_P = "preprocessor";

  /**
   * Flag for omission of preprocessing.
   */
  public static final String OMIT_PREPROCESSING_F = "omitPreprocessing";

  /**
   * Description for flag for force of preprocessing.
   * TODO: description: matrix?
   */
  public static final String OMIT_PREPROCESSING_D = "flag to omit (a new) preprocessing if for each object a matrix already has been associated.";

  public static final Flag OMIT_PREPROCESSING_FLAG = new Flag(OMIT_PREPROCESSING_F, OMIT_PREPROCESSING_D);
  
  /**
   * True, if preprocessing is omitted, false otherwise.
   */
  private boolean omit;

  /**
   * The preprocessor to run the variance analysis of the objects.
   */
  private P preprocessor;

  /**
   * The assocoiation ID for the association to be set by the preprocessor
   */
  private AssociationID associationID;

  /**
   * The super class for the preprocessor class.
   */
  private Class<? extends Preprocessor> preprocessorSuperClassName;

  /**
   * The description for the preprocessor class.
   */
  private String preprocessorClassDescription;

  /**
   * Indicates if the verbose flag is set for preprocessing..
   */
  private boolean verbose;

  /**
   * Indicates if the time flag is set for preprocessing.
   */
  private boolean time;

  /**
   * The database to run the preprocessor on.
   */
  private Database<O> database;

  /**
   * Provides a handler class for all distance function using a preprocessor.
   *
   * @param optionHandler                the option handler for parameter handling.
   * @param preprocessorClassDescription the description for the preprocessor class
   * @param preprocessorSuperClassName   the super class for the preprocessor class
   * @param defaultPreprocessorClassName the class name for the default preprocessor class
   * @param associationID                the assocoiation ID for the association to be set by the preprocessor
   */
  public PreprocessorHandler(OptionHandler optionHandler,
                             String preprocessorClassDescription,
                             Class<P> preprocessorSuperClassName,
                             String defaultPreprocessorClassName,
                             AssociationID associationID) {

    this.associationID = associationID;
    this.preprocessorSuperClassName = preprocessorSuperClassName;
    this.preprocessorClassDescription = preprocessorClassDescription;

    // omit flag
    optionHandler.put(PreprocessorHandler.OMIT_PREPROCESSING_FLAG);

    // preprocessor
    ClassParameter<P> prepClass = new ClassParameter<P> 
    											(PreprocessorHandler.PREPROCESSOR_CLASS_P,
                                                preprocessorClassDescription,
                                                preprocessorSuperClassName);
    prepClass.setDefaultValue(defaultPreprocessorClassName);
    optionHandler.put(prepClass);
  }

  /**
   * Sets the values for the parameters delta and preprocessor if specified.
   * If the parameters are not specified default values are set.
   *
   * @see de.lmu.ifi.dbs.utilities.optionhandling.Parameterizable#setParameters(String[])
   */
  public String[] setParameters(OptionHandler optionHandler, String[] parameters) throws ParameterException {
    // preprocessor
    String prepClassString = optionHandler.getOptionValue(PREPROCESSOR_CLASS_P);

    try {
      // noinspection unchecked
        // todo
      preprocessor = (P) Util.instantiate(preprocessorSuperClassName, prepClassString);
    }
    catch (UnableToComplyException e) {
      throw new WrongParameterValueException(PREPROCESSOR_CLASS_P,
                                             prepClassString,
                                             preprocessorClassDescription,
                                             e);
    }

    // omit
    omit = optionHandler.isSet(OMIT_PREPROCESSING_F);

    return preprocessor.setParameters(parameters);
  }

  /**
   * Adds the parameter settings of the preprocessor to the specified list.
   *
   * @param settingsList the list of parameter settings to add the parameter settings
   *             of the preprocessor to
   */
  public void addAttributeSettings(List<AttributeSettings> settingsList) {
    settingsList.addAll(preprocessor.getAttributeSettings());
  }

 /**
   * Invoked after objects of the database have been updated in some way.
   * Use <code>e.getObjects()</code> to get the updated database objects.
   */
  public void objectsChanged(DatabaseEvent e) {
    if (!omit) {
      preprocessor.run(database, verbose, time);
    }
  }

  /**
   * Invoked after an object has been inserted into the database.
   * Use <code>e.getObjects()</code> to get the newly inserted database objects.
   */
  public void objectsInserted(DatabaseEvent e) {
    if (!omit) {
      preprocessor.run(database, verbose, time);
    }
  }

  /**
   * Invoked after an object has been deleted from the database.
   * Use <code>e.getObjects()</code> to get the inserted database objects.
   */
  public void objectsRemoved(DatabaseEvent e) {
    if (!omit) {
      preprocessor.run(database, verbose, time);
    }
  }

  /**
   * Returns the preprocessor of this distance function.
   *
   * @return the preprocessor of this distance function
   */
  public P getPreprocessor() {
    return preprocessor;
  }

  /**
   * Runs the preprocessor on the database if
   * the omit flag is not set or the database does contain the
   * association id neither for any id nor as global association.
   *
   * @param database the database to run the preprocessor on
   * @param verbose  flag to allow verbose messages while performing the method
   * @param time     flag to request output of performance time
   */
  public void runPreprocessor(Database<O> database, boolean verbose, boolean time) {
    this.database = database;
    this.verbose = verbose;
    this.time = time;
    if (!omit ||
            !( (database.isSet(associationID)
                || database.isSetGlobally(associationID) )
                )
       )
                {
      preprocessor.run(database, verbose, time);
    }
    database.addDatabaseListener(this);
  }
}
