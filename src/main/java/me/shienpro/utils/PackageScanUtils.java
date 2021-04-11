package me.shienpro.utils;

import me.shienpro.excepiton.PackageScanException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;

public class PackageScanUtils {
    public static Set<String> scanPackage(Class<?> mainClass) {
        Set<String> classNameSet = new HashSet<>();
        ClassLoader cl = PackageScanUtils.class.getClassLoader();

        String pkg = mainClass.getPackage().getName();
        String path = pkg.replaceAll("\\.", Matcher.quoteReplacement(File.separator));

        try {
            // current jar
            URL mainURL = mainClass.getResource(mainClass.getSimpleName() + ".class");
            Objects.requireNonNull(mainURL);
            classNameSet.addAll(scanUrl(mainURL, pkg, path));

            // classpath
            Enumeration<URL> resources = cl.getResources(path);
            while (resources.hasMoreElements()) {
                classNameSet.addAll(scanUrl(resources.nextElement(), pkg, path));
            }

            return classNameSet;
        } catch (IOException e) {
            throw new PackageScanException(e);
        }
    }

    private static Set<String> scanUrl(URL url, String pkg, String path) throws IOException {
        Set<String> classNameSet = new HashSet<>();

        String filePath = url.getPath();
        if (filePath.contains("%")) {
            filePath = URLDecoder.decode(filePath, "utf-8");
        }

        if ("jar".equals(url.getProtocol())) {
            String prefix = "file:/";
            String jarPath;
            jarPath = filePath.substring(filePath.indexOf(prefix) + prefix.length(), filePath.indexOf("!"));

            classNameSet.addAll(scanJar(jarPath, path));
            return classNameSet;
        }

        if ("file".equals(url.getProtocol())) {
            String dirPath = filePath.startsWith("/") ? filePath.substring(1) : filePath;
            classNameSet.addAll(scanDir(dirPath, pkg));
        }

        return classNameSet;
    }

    private static Set<String> scanJar(String jarPath, String pkgPath) throws IOException {
        Set<String> classNameSet = new HashSet<>();

        pkgPath = pkgPath.replaceAll(Matcher.quoteReplacement(File.separator), "/");

        JarFile jarFile = new JarFile(jarPath);
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.startsWith(pkgPath) && name.endsWith(".class") && !name.contains("$")) {
                classNameSet.add(name.substring(0, name.indexOf(".class")).replaceAll("/", "."));
            }
        }

        return classNameSet;
    }

    private static Set<String> scanDir(String filePath, String pkg) {
        Set<String> classNameSet = new HashSet<>();
        File dir = new File(filePath);
        File[] files;

        if (!dir.isDirectory() || (files = dir.listFiles()) == null) {
            return classNameSet;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                classNameSet.addAll(scanDir(file.getPath(), pkg + "." + file.getName()));
                continue;
            }
            if (!file.getName().endsWith(".class") || file.getName().contains("$")) {
                continue;
            }

            classNameSet.add(pkg + "." + file.getName().substring(0, file.getName().indexOf(".class")));
        }

        return classNameSet;
    }
}
