package io.f04.code.ringcode.app;

import io.f04.code.ringcode.constants.Constants;
import io.f04.code.ringcode.decode.Reader;
import io.f04.code.ringcode.encode.Generator;
import io.f04.code.ringcode.exceptions.BadDataSizeException;
import io.f04.code.ringcode.exceptions.BadMetadataException;
import io.f04.code.ringcode.picture.CapturedImage;
import io.f04.code.ringcode.picture.Capturer;
import org.apache.commons.cli.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RingCodeUtils {

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption("help", false, "Help information.")
                .addOption("test", false, "Debug mode.")
                .addOption("g", true, "Generate ring code picture.")
                .addOption("c", false, "Colored ring code picture. Require with -g")
                .addOption("f", true, "Ring code picture format. Require with -g")
                .addOption("r", false, "Read the raw ring code picture.")
                .addOption("p", false, "Process ring code photo.");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options,args);

        if (cmd.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "java RingCodeUtils.jar [OPTION] <FILENAME>", options );
            return;
        }
        if (cmd.hasOption("g")) {
            Generator generator = new Generator();
            generator.setData(cmd.getOptionValue("g"));
            if (cmd.hasOption("c")) {
                generator.setRingColors(Constants.RING_COLORS);
            }
            generator.toFile(new File(cmd.getArgs()[0]), cmd.getOptionValue("f"));
            return;
        }
        if (cmd.hasOption("r")) {
            try {
                readRawPicture(cmd.hasOption("test"), cmd.getArgs()[0]);
            } catch (Exception e) {
                System.err.println("Read raw picture fail!");
                e.printStackTrace();
            }
            return;
        }
        if (cmd.hasOption("p")) {
            try {
                capturePhoto(cmd.hasOption("test"), cmd.getArgs()[0]);
            } catch (Exception e) {
                System.err.println("Capture photo fail!");
                e.printStackTrace();
            }
            return;
        }

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "java RingCodeUtils.jar [OPTION] <FILENAME>", options );
    }

    private static void readRawPicture(boolean shouldDebug, String fileName) throws IOException,
            BadMetadataException, BadDataSizeException {
        Reader reader = new Reader();
        reader.setTest(shouldDebug);
        if (!reader.isTest()) {
            reader.setSampleTimes(23);
        }
        reader.setFileName(fileName);
        BufferedImage img = ImageIO.read(new File(reader.getFileName()));
        byte[] data = reader.read(img, img.getWidth() / 2, img.getHeight() / 2);
        System.out.println(new String(data, StandardCharsets.UTF_8));
    }

    private static void capturePhoto(boolean shouldDebug, String fileName) throws IOException,
            BadMetadataException, BadDataSizeException {
        Capturer capturer = new Capturer();
        capturer.setTest(shouldDebug);
        List<CapturedImage> images = capturer.capture(new File(fileName));
        if (images.isEmpty()) {
            System.err.println("[ERR] No ring code in photo!");
            return;
        }

        CapturedImage image = images.get(0);

        Reader reader = new Reader();
        reader.setTest(shouldDebug);

        int w = image.getImage().getWidth() / 2;
        reader.setRingWidth(image.approximateRingWidth());

        if (!reader.isTest()) {
            reader.setSampleTimes(23);
        }
        reader.setFileName(capturer.getInputFile().toString());
        byte[] data = reader.read(image.getImage(), w, w);
        System.out.println(new String(data, StandardCharsets.UTF_8));
    }
}
