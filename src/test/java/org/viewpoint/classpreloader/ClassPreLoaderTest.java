package org.viewpoint.classpreloader;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

public class ClassPreLoaderTest {
    static boolean isSuccess = false;

    @Test
    public void loadForPreLoadableTest() throws Exception {
        ClassPreLoader loader = new ClassPreLoader("org.viewpoint");
        List<Class> classes = loader.loadForPreLoadable();
        Optional<Class> pltCls = classes.stream().filter(cls -> cls.equals(PreLoadableTest.class)).findAny();

        Assert.assertTrue(pltCls.isPresent());
        Assert.assertEquals(PreLoadableTest.class, pltCls.orElse(Object.class));
        Assert.assertTrue(isSuccess);
    }

    @PreLoadable
    static class PreLoadableTest {
        static {
            System.out.println("Call PreloadableTest static initializer");
            isSuccess = true;
        }
    }

    static class LoadTest {
        static {
            System.out.println("Call LoadTest static initializer");
            isSuccess = true;
        }
    }
}