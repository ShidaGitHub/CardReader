package com.ord.cardreader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.logging.Logger;

/*
 * pHash-like image hash.
 * Based On: http://www.hackerfactor.com/blog/index.php?/archives/432-Looks-Like-It.html
 */

public class ImageCash {
    private static final Logger LOGGER = Logger.getLogger(ImageCash.class.getName());

    private static final int SIZE = 32;
    private int SMALLER_SIZE = 8;

    private double[] c;

    public ImageCash(){
        // http://stackoverflow.com/questions/4240490/problems-with-dct-and-idct-algorithm-in-java
        c = new double[SIZE];
        Arrays.fill(c, 1);
        c[0] = 1 / Math.sqrt(2.0);
        ImageIO.setUseCache(false);
    }

    /**
     * @param img Image to hash
     * @return a 'binary string' (like. 001010111011100010) which is easy to do a hamming distance on.
     * @throws IOException
     */
    public String getHash(BufferedImage img) throws IOException {
        //1. Reduce size.
        BufferedImage resizedImage = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(img, 0, 0, SIZE, SIZE, null);
        g.dispose();

        //2. Reduce color
        ColorConvertOp colorConvert = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        img = colorConvert.filter(resizedImage, resizedImage);

        double values[][] = new double[SIZE][SIZE];
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                values[x][y] = (img.getRGB(x, y)) & 0xff; //getBlue
            }
        }

        //3. Compute the DCT.
        double[][] dctVals = new double[SIZE][SIZE];
        for (int u = 0; u < SIZE; u++) {
            for (int v = 0; v < SIZE; v++) {
                double sum = 0.0;
                for (int i = 0; i < SIZE; i++) {
                    for (int j = 0; j < SIZE; j++)
                        sum += Math.cos(((2 * i + 1) / (2.0 * SIZE)) * u * Math.PI) * Math.cos(((2 * j + 1) / (2.0 * SIZE)) * v * Math.PI) * (values[i][j]);
                }
                sum *= ((c[u] * c[v]) / 4.0);
                dctVals[u][v] = sum;
            }
        }

        //4. Reduce the DCT.
        //5. Compute the average value.
        double total = 0;
        for (int x = 0; x < SMALLER_SIZE; x++) {
            for (int y = 0; y < SMALLER_SIZE; y++)
                total += dctVals[x][y];
        }
        total -= dctVals[0][0];
        double avg = total / (double) ((SMALLER_SIZE * SMALLER_SIZE) - 1);

        //6. Further reduce the DCT.
        StringBuilder sbHash = new StringBuilder();
        for (int x = 0; x < SMALLER_SIZE; x++) {
            for (int y = 0; y < SMALLER_SIZE; y++) {
                sbHash.append(dctVals[x][y] > avg ? "1" : "0");
            }
        }
        return sbHash.toString();
    }

    /**
     * show count different chars between s1 and s2
     * @param s1 - the first string
     * @param s2 - the second string
     * @return count different chars
     */
    public int distance(String s1, String s2) {
        if (s1 == null || s2 == null) return 100;
        if (s1.length() != s2.length()) return 100;

        int counter = 0;
        for (int k = 0; k < s1.length(); k++) {
            if (s1.charAt(k) != s2.charAt(k)) {
                counter++;
            }
        }
        return counter;
    }
}
