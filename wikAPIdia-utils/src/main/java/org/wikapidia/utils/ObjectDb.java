package org.wikapidia.utils;


import com.sleepycat.je.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.util.Iterator;
import java.util.logging.Logger;


/**
 * A key / value database where keys are strings and objects are serializable.
 *
 */
public class ObjectDb<V extends Serializable> implements Iterable<Pair<String, V>> {
    private static final Logger LOG = Logger.getLogger(ObjectDb.class.getName());
    private Environment env;
    private Database db;

    public ObjectDb(File path) throws IOException, DatabaseException {
        this(path, false);
    }

    /**
     * Creates a new object database.
     * @param path Path to the directory containing the dictionary.
     * @param isNew If true, resets the mapper database.
     * @throws java.io.IOException
     * @throws DatabaseException
     */
    public ObjectDb(File path, boolean isNew) throws IOException, DatabaseException {
        if (isNew) {
            if (path.isDirectory()) {
                FileUtils.deleteDirectory(path);
            } else if (path.isFile()) {
                path.delete();
            }
            path.mkdirs();
        }
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setTransactional(false);
        envConfig.setAllowCreate(true);
        this.env = new Environment(path, envConfig);

        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(true);
        this.db = env.openDatabase(null,
                FilenameUtils.getName(path.toString()),
                dbConfig);
    }

    /**
     * Returns the value associated with the key, or null if none exists.
     * @param key
     * @return
     * @throws DatabaseException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public V get(String key) throws DatabaseException, IOException, ClassNotFoundException {
        DatabaseEntry current = new DatabaseEntry();
        DatabaseEntry entryKey = new DatabaseEntry(key.getBytes("UTF-8"));
        OperationStatus status = db.get(null, entryKey, current, null);
        if (status.equals(OperationStatus.NOTFOUND)) {
            return null;
        } else {
            return (V) WpIOUtils.bytesToObject(current.getData());
        }
    }

    /**
     * Add or replace an entry in the object database.
     * @param key
     * @param record
     * @throws DatabaseException
     */
    public void put(String key, V record) throws DatabaseException, IOException {
        db.put(null,
                new DatabaseEntry(key.getBytes("UTF-8")),
                new DatabaseEntry(WpIOUtils.objectToBytes(record)));
    }

    /**
     * Close and flush the concept database.
     * @throws DatabaseException
     */
    public void close() throws DatabaseException {
        this.db.close();
        this.env.close();
    }

    public void flush() {
        this.env.flushLog(true);
    }

    public void remove(String key) throws UnsupportedEncodingException, DatabaseException {
        this.db.delete(null, new DatabaseEntry(key.getBytes("UTF-8")));
    }

    /**
     * Gets the underlying database.
     * @return
     */
    public Database getDb() {
        return db;
    }

    /**
     * Gets the underlying database environment.
     * @return
     */
    public Environment getEnvironment() {
        return env;
    }

    /**
     * Iterate over key / value pairs.
     * The cursor is closed when all the pairs have been read or when there is an error.
     * Otherwise the cursor is not closed... this is bad!
     * @return an iterator over key / value pairs.
     */
    @Override
    public Iterator<Pair<String, V>> iterator() {
        final DatabaseEntry key = new DatabaseEntry();
        final DatabaseEntry val = new DatabaseEntry();
        final Cursor cursor;
        try {
            cursor = this.db.openCursor(null, CursorConfig.READ_UNCOMMITTED);
        } catch (DatabaseException e) {
            throw new RuntimeException(e);  // what else to do?
        }
        return new Iterator<Pair<String, V>>() {
            boolean finished = false;
            boolean hasValue = false;

            @Override
            public boolean hasNext() {
                advance();
                return !finished;
            }

            @Override
            public Pair<String, V> next() {
                advance();
                if (finished) return null;
                hasValue = false;
                try {
                    return Pair.of(
                            new String(key.getData(), "UTF-8"),
                            (V)WpIOUtils.bytesToObject(val.getData())
                    );
                } catch (IOException e) {
                    close();
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    close();
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void remove() {
                try {
                    cursor.delete();
                } catch (DatabaseException e) {
                    throw new RuntimeException(e);
                }
            }

            private void advance() {
                if (finished || hasValue) return;
                try {
                    if (cursor.getNext(key, val, LockMode.READ_UNCOMMITTED) != OperationStatus.SUCCESS) {
                        close();
                        finished = true;
                    }
                } catch (DatabaseException e) {
                    close();
                    throw new RuntimeException(e);
                }
                hasValue = true;
            }

            private void close() {
                try { cursor.close(); } catch (DatabaseException e) {}
            }
        };
    }
}
