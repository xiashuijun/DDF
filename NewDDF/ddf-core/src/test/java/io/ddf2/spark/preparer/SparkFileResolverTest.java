package io.ddf2.spark.preparer;

import io.ddf2.datasource.fileformat.JSonFile;
import io.ddf2.datasource.fileformat.ParquetFile;
import io.ddf2.datasource.fileformat.TextFileFormat;
import io.ddf2.datasource.filesystem.LocalFileDataSource;
import io.ddf2.datasource.schema.ISchema;
import org.apache.spark.sql.hive.HiveContext;
import org.junit.Test;
import utils.TestUtils;

/**
 * Created by sangdn on 1/7/16.
 */
public class SparkFileResolverTest {

    HiveContext hiveContext = TestUtils.getHiveContext();
    SparkFileResolver resolver = new SparkFileResolver(null, hiveContext);

    @Test
    public void testCSVResolver() throws Exception {
        String pathUserData = "/tmp/Data4SparkResolver.dat";
        TestUtils.makeCSVFileUserInfo(pathUserData, 10, TestUtils.COMMA_SEPARATOR);


        LocalFileDataSource localFileDataSource = LocalFileDataSource.builder()
                .addPath(pathUserData)
                .setFileFormat(new TextFileFormat(TextFileFormat.COMMA_SEPARATOR))
                .build();
        ISchema schema = resolver.resolve(localFileDataSource);
        System.out.println("Schema After Resolver");
        System.out.println(schema);


    }
    @Test
    public void testParquetResolver() throws Exception {
        String pathUserData = "/tmp/Data4SparkResolver.pqt";
        TestUtils.makeParquetFileUserInfo(hiveContext, pathUserData, 10);

        LocalFileDataSource localFileDataSource = LocalFileDataSource.builder()
                .addPath(pathUserData)
                .setFileFormat(new ParquetFile())
                .build();
        ISchema schema = resolver.resolve(localFileDataSource);
        System.out.println("Schema After Resolver");
        System.out.println(schema);


    }
    @Test
    public void testJSONResolver() throws Exception {
        String pathUserData = "/tmp/Data4SparkResolver.json";
        TestUtils.makeJSONFileUserInfo(pathUserData, 10);


        LocalFileDataSource localFileDataSource = LocalFileDataSource.builder()
                .addPath(pathUserData)
                .setFileFormat(new JSonFile())
                .build();
        ISchema schema = resolver.resolve(localFileDataSource);
        System.out.println("Schema After Resolver");
        System.out.println(schema);


    }
}