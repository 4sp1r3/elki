package experimentalcode.frankenb.model.ifaces;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.utilities.pairs.Pair;

/**
 * No description given.
 * 
 * @author Florian Frankenberger
 */
public interface IPartition extends Iterable<Pair<Integer, NumberVector<?, ?>>> {

  public int getId();
  
  public File getStorageFile();

  public void addVector(int id, NumberVector<?, ?> vector);

  public void close() throws IOException;

  public Iterator<Pair<Integer, NumberVector<?, ?>>> iterator();

  public int getSize();

  public int getDimensionality();

  public void copyTo(File file) throws IOException;

}