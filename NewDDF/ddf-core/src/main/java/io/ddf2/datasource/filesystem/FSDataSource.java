package io.ddf2.datasource.filesystem;

import io.ddf2.datasource.IDataSource;

public class FSDataSource extends FileDataSource {

    /**
     * @see IDataSource#getNumColumn()
     */
    @Override
    public int getNumColumn() {
        return 0;
    }
}
 
