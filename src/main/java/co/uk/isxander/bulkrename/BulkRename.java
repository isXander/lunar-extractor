package co.uk.isxander.bulkrename;

import co.uk.isxander.bulkrename.utils.FileUtils;
import com.google.common.collect.ImmutableList;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.function.Consumer;

public class BulkRename {

    public static void main(String[] args) throws Exception {
        long fullStart = System.currentTimeMillis();
        long start = fullStart;
        long finish;

        File inputJar = new File("./lunar-compiled.jar");
        if (!inputJar.exists()) {
            int input = JOptionPane.showOptionDialog(null, "Make sure there is a file named: \"lunar-compiled.jar\" in the directory.", "Could not complete", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
            System.exit(0);
        }
        File inputZip = new File("./lunar-compiled.zip");
        Files.copy(inputJar.toPath(), inputZip.toPath());
        FileUtils.unzipFile(inputZip, new File("./lunar-extracted"));

        finish = System.currentTimeMillis();
        System.out.println("\n\nExtract Process Took " + new DecimalFormat("0.0").format(finish - start) + " ms");
        start = System.currentTimeMillis();

        ImmutableList.Builder<File> builder = ImmutableList.builder();
        fetchFiles(new File("./lunar-extracted"), builder::add);
        ImmutableList<File> files = builder.build();

        files.stream().parallel().forEach((f) -> {
            if (f.getName().endsWith(".lclass")) {
                int index = f.getName().lastIndexOf('.');
                String name = f.getName().substring(0, index);
                File newFile = new File(f.getPath().substring(0, f.getPath().length() - f.getName().length()) + "/" + name + ".class");
                System.out.println(f.getAbsolutePath() + " -> " + newFile.getAbsolutePath());
                f.renameTo(newFile);
            }
        });

        finish = System.currentTimeMillis();
        System.out.println("\n\nRename Process Took " + new DecimalFormat("0.0").format(finish - start) + " ms");
        start = System.currentTimeMillis();

        builder = ImmutableList.builder();
        fetchFiles(new File("./lunar-extracted"), builder::add);
        files = builder.build();


        FileUtils.zipDir(new File("./lunar-extracted"), new File("./lunar-recomp.jar"));

        finish = System.currentTimeMillis();
        System.out.println("\n\nArchive Process Took " + new DecimalFormat("0.0").format(finish - start) + " ms");

        inputZip.delete();

        System.out.println("\n\nFinished. Overall time: " + new DecimalFormat("0.0").format(System.currentTimeMillis() - fullStart) + " ms");

        JOptionPane.showMessageDialog(null, "Finished Extraction, Modification and Archival Process.", "Finished", JOptionPane.PLAIN_MESSAGE);
    }

    public static void fetchFiles(File dir, Consumer<File> fileConsumer) {
        if (dir.isDirectory()) {
            for (File f : dir.listFiles()) {
                fetchFiles(f, fileConsumer);
            }
        } else {
            fileConsumer.accept(dir);
        }
    }

}
