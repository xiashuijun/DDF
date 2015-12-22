package io.newddf;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Created by sangdn on 12/21/15.
 */
public abstract class NewDDFManager {

    protected NewDDFManager(Map params){

    }
    public static  <T extends NewDDFManager> T newInstance(Class<T> ddfManager,Map params){
        try {
            Constructor<T> constructors = ddfManager.getDeclaredConstructor(Map.class);
            constructors.setAccessible(true);
            return constructors.newInstance(params);
        } catch (InstantiationException |IllegalAccessException|InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


}
