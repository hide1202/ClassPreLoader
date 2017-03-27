package org.viewpoint.classpreloader;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

public class ClassPreLoaderTest {
    static boolean isSuccess = false;

    @PreLoadable
    static class PreLoadableTest {
        static {
            isSuccess = true;
        }
    }

    @Test
    public void loadForPreLoadableTest() throws Exception {
        ClassPreLoader loader = new ClassPreLoader("org.viewpoint");
        List<Class> classes = loader.loadForPreLoadable();
        Optional<Class> pltCls = classes.stream().filter(cls -> cls.equals(PreLoadableTest.class)).findAny();

        Assert.assertTrue(pltCls.isPresent());
        Assert.assertEquals(PreLoadableTest.class, pltCls.orElse(Object.class));
        Assert.assertTrue(isSuccess);
    }
}