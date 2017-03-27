package org.viewpoint.classpreloader;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassPreLoader {
    private String prefix;

    public ClassPreLoader(String packagePrefix) {
        this.prefix = packagePrefix;
    }

    private static <T> Stream<T> skipOptionalEmpty(Optional<T> optional) {
        return optional.map(Stream::of).orElseGet(Stream::empty);
    }

    private static List<URL> resourceUrlList(String resourceName) {
        try {
            Enumeration<URL> resources = Thread.currentThread()
                    .getContextClassLoader()
                    .getResources(resourceName);
            return Collections.list(resources);
        } catch (IOException ignored) {
            return new ArrayList<>();
        }
    }

    private static Stream<File> directoriesFromUrls(List<URL> urlList) {
        return urlList.stream()
                .map(r -> {
                    try {
                        return Optional.of(new File(r.toURI()));
                    } catch (URISyntaxException e) {
                        return Optional.<File>empty();
                    }
                })
                .flatMap(ClassPreLoader::skipOptionalEmpty);
    }

    private static Stream<Path> classFilePathListFromFiles(Stream<File> fileStream) {
        return fileStream
                .flatMap(dir -> {
                    try {
                        return Files.walk(dir.toPath(), FileVisitOption.FOLLOW_LINKS);
                    } catch (IOException ignored) {
                    }
                    return Stream.empty();
                })
                .filter(p -> p.toAbsolutePath().toString().endsWith(".class"));
    }

    private static String dotToSlash(String src) {
        return src.replace('.', '/');
    }

    private Optional<String> classNameFromFilePath(String src) {
        String replacePackage = dotToSlash(prefix);
        int i = src.indexOf(replacePackage);
        if (i <= -1)
            return Optional.empty();
        String startWithPackageName = src.substring(i).replace('/', '.');
        int lastDotClassIndex = startWithPackageName.lastIndexOf(".class");
        return Optional.of(startWithPackageName.substring(0, lastDotClassIndex));
    }

    public List<Class> load(Class anotationCls) {
        List<URL> resources = resourceUrlList(dotToSlash(prefix));
        Stream<File> dirStream = directoriesFromUrls(resources);
        Stream<Path> classFilePathStream = classFilePathListFromFiles(dirStream);

        return classFilePathStream
                .map(p -> p.toFile().getAbsolutePath())
                .map(this::classNameFromFilePath)
                .flatMap(ClassPreLoader::skipOptionalEmpty)
                .map(className -> {
                    try {
                        Class<?> clazz = Class.forName(className);
                        if (clazz != null && clazz.isAnnotationPresent(anotationCls)) {
                            return Optional.of(clazz);
                        }
                        return Optional.<Class>empty();
                    } catch (ClassNotFoundException ignored) {
                        ignored.printStackTrace();
                    }
                    return Optional.<Class>empty();
                })
                .flatMap(ClassPreLoader::skipOptionalEmpty)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<Class> loadForPreLoadable() {
        return load(PreLoadable.class);
    }
}
