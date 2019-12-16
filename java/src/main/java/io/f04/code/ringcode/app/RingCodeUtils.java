package io.f04.code.ringcode.app;

import io.f04.code.ringcode.constants.Constants;
import io.f04.code.ringcode.decode.Reader;
import io.f04.code.ringcode.encode.Generator;
import io.f04.code.ringcode.enums.ErrorCorrectionLevel;
import io.f04.code.ringcode.exceptions.BadDataSizeException;
import io.f04.code.ringcode.exceptions.BadMetadataException;
import io.f04.code.ringcode.picture.CapturedImage;
import io.f04.code.ringcode.picture.Capturer;
import org.apache.commons.cli.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RingCodeUtils {

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption("help", false, "😳 Help information.")
                .addOption("test", false, "🛠 Debug mode.")
                .addOption("level", true, "❗️ Error correction level(L/M/Q/H).")
                .addOption("g", true, "🏞 Generate ring code picture.")
                .addOption("c", false, "🌈 Colored ring code picture. Require with -g.")
                .addOption("f", true, "📄 Ring code picture format. Require with -g.")
                .addOption("r", false, "📤 Read the raw ring code picture.")
                .addOption("p", false, "📷 Read ring code in photo.");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options,args);

        if (cmd.hasOption("help")) {
            usage(options);
            return;
        }
        if (cmd.hasOption("g")) {
            try {
                generate(cmd.hasOption("c"), cmd.getOptionValue("g"), cmd.getOptionValue("level"),
                        cmd.getOptionValue("f"), cmd.getArgs()[0]);
            } catch (Exception e) {
                System.err.println("Generate picture fail!");
                e.printStackTrace();
            }
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

        usage(options);
    }

    private static void usage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "java -jar ring-code-utils.jar [OPTION] <FILENAME>", options );
    }

    private static void generate(boolean colored, String data, String level, String format, String fileName)
            throws IOException, BadDataSizeException {
        Generator generator = new Generator();
        generator.setData(data);
        if (colored) {
            generator.setRingColors(Constants.RING_COLORS);
        }
        switch (level == null ? "" : level) {
            case "L":
                generator.setErrorCorrectionLevel(ErrorCorrectionLevel.L);
                break;
            case "M":
                generator.setErrorCorrectionLevel(ErrorCorrectionLevel.M);
                break;
            case "Q":
                generator.setErrorCorrectionLevel(ErrorCorrectionLevel.Q);
                break;
            case "H":
                generator.setErrorCorrectionLevel(ErrorCorrectionLevel.H);
                break;
            case "":
                break; // ignore
            default:
                throw new IllegalArgumentException("Unknown Error Correction Level: " + level);

        }
        generator.toFile(new File(fileName), format);
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
