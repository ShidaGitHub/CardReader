package com.ord.cardreader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
*   Extract card as sub image from general image.
*   Extracting from left to right before the first empty card
*/

public class CardExtract {
    public static final Color EMPTY_CARD_COLOR = new Color(60, 60, 60); //check color for empty card
    public static final int DEFAULT_START_X = 148;
    public static final int DEFAULT_START_Y = 591;
    public static final int DEFAULT_WIDTH = 55;
    public static final int DEFAULT_HEIGHT = 77;
    public static final int DEFAULT_OFFSET_X = 71;

    private int startX, startY, width, height, offsetX;

    public CardExtract(){
        this(DEFAULT_START_X, DEFAULT_START_Y, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_OFFSET_X);
    }

    /**
     * @param startX start X position in general image. Indicated for the first card
     * @param startY start Y position in general image. Indicated for the first card
     * @param width width of card
     * @param height height of card
     * @param offsetX offset from startX coordinate for calculation startX next card. Not include width of card
     */
    public CardExtract(int startX, int startY, int width, int height, int offsetX){
        this.startX = startX; this.startY = startY;
        this.width = width; this.height = height;
        this.offsetX = offsetX;
    }

    public int getStartX() {
        return startX;
    }
    public void setStartX(int startX) {
        this.startX = startX;
    }

    public int getStartY() {
        return startY;
    }
    public void setStartY(int startY) {
        this.startY = startY;
    }

    public int getWidth() {
        return width;
    }
    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }
    public void setHeight(int height) {
        this.height = height;
    }

    public int getOffsetX() {
        return offsetX;
    }
    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    /**
     * Extract card as sub image from general image.
     * Extracting from left to right before the first empty card
     * @param src general image
     * @return list of card
     */
    public List<BufferedImage> getCards(BufferedImage src){
        List<BufferedImage> cardList = new ArrayList<>();
        if (src == null) return cardList;
        for (int xi = startX; xi < src.getWidth(); xi += offsetX){
            BufferedImage card = src.getSubimage(xi, startY, width, height);
            if (cardIsEmpty(card)) break;
            cardList.add(cropOnContent(card));
        }
        return cardList;
    }


    /**
     * @param img source image
     * @return content image
     */
    private BufferedImage cropOnContent(BufferedImage img){
        //get background from right top corner
        int[] backPixels = img.getRGB(img.getWidth() - 7, 5, 2, 2, null, 0, 2);
        Color background = getAverageColor(backPixels);

        //get coordinates
        int startX = 0;
        int startY = 0;
        int width = 0;
        int height = 0;

        //left border
        for (int leftRow = 0; leftRow < img.getWidth() - 1; leftRow++){
            int[] rgbPixels = img.getRGB(leftRow, 0, 1, img.getHeight(), null, 0, 1);
            Color minPx = new Color(Arrays.stream(rgbPixels).min().orElse(0));

            if (Math.abs(minPx.getRed() - background.getRed()) > 30 ||
                    Math.abs(minPx.getGreen() - background.getGreen()) > 30 ||
                    Math.abs(minPx.getBlue() - background.getBlue()) > 30){

                startX = leftRow;
                break;
            }
        }

        //top border
        for (int topRow = 0; topRow < img.getHeight(); topRow++){
            int[] rgbPixels = img.getRGB(0, topRow, img.getWidth(), 1, null, 0, img.getWidth());
            Color minPx = new Color(Arrays.stream(rgbPixels).min().orElse(0));
            if (Math.abs(minPx.getRed() - background.getRed()) > 30 ||
                    Math.abs(minPx.getGreen() - background.getGreen()) > 30 ||
                    Math.abs(minPx.getBlue() - background.getBlue()) > 30){

                startY = topRow;
                break;
            }
        }

        //width
        for (int rightRow = img.getWidth()-1; rightRow > 0; rightRow--){
            int[] rgbPixels = img.getRGB(rightRow, 0, 1, img.getHeight(), null, 0, 1);
            Color minPx = new Color(Arrays.stream(rgbPixels).min().orElse(0));
            if (Math.abs(minPx.getRed() - background.getRed()) > 30 ||
                    Math.abs(minPx.getGreen() - background.getGreen()) > 30 ||
                    Math.abs(minPx.getBlue() - background.getBlue()) > 30){

                width = rightRow - startX;
                break;
            }
        }

        //height
        for (int bottomRow = img.getHeight() - 1; bottomRow > 0; bottomRow--){
            int[] rgbPixels = img.getRGB(0, bottomRow, img.getWidth(), 1, null, 0, img.getWidth());
            Color minPx = new Color(Arrays.stream(rgbPixels).min().orElse(0));
            if (Math.abs(minPx.getRed() - background.getRed()) > 30 ||
                    Math.abs(minPx.getGreen() - background.getGreen()) > 30 ||
                    Math.abs(minPx.getBlue() - background.getBlue()) > 30){

                height = bottomRow - startY;
                break;
            }
        }
        return img.getSubimage(startX, startY, width, height);
    }

    /**
     * Get 4 pixels in left bottom border, build average color and compare with EMPTY_CARD_COLOR
     * @param card check image
     * @return if card is not empty then true else false
     */
    private boolean cardIsEmpty(BufferedImage card){
        int[] rgbPixels = card.getRGB(0, card.getHeight() - 2, 2, 2, null, 0, 2);
        return getAverageColor(rgbPixels).getRGB() < EMPTY_CARD_COLOR.getRGB();
    }

    private Color getAverageColor(int[] rgbPixels){
        int redBucket = Arrays.stream(rgbPixels).reduce(0, (x,y) -> x + ((y >> 16) & 0xFF));
        int greenBucket = Arrays.stream(rgbPixels).reduce(0, (x,y) -> x + ((y >> 8) & 0xFF));
        int blueBucket = Arrays.stream(rgbPixels).reduce(0, (x,y) -> x + (y & 0xFF));

        return new Color(redBucket / rgbPixels.length,greenBucket / rgbPixels.length,blueBucket / rgbPixels.length);
    }
}
