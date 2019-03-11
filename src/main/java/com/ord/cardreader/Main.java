package com.ord.cardreader;

import org.yaml.snakeyaml.Yaml;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    static {
        try {
            LogManager.getLogManager().readConfiguration(Main.class.getResourceAsStream("logging.properties"));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can't load logging.properties from resources!");
        }
    }

    private ImageCash imageCash;
    private CardExtract cardExtract;
    private HashMap<String, String[]> deck = new HashMap();

    public Main(){
        imageCash = new ImageCash();
        cardExtract = new CardExtract();
        initDeck();
    }

    /**
     * fill deck from deck.yml.
     */
    private void initDeck(){
        Yaml yaml = new Yaml();
        InputStream inputStream = Main.class.getResourceAsStream("deck.yml");
        LinkedHashMap<String, Object> ymlDeck = yaml.load(inputStream);

        if (ymlDeck != null)
            ymlDeck.keySet().forEach(key ->
                    deck.put(key, String.valueOf(ymlDeck.get(key)).
                                  replaceAll("[\\D][\\-]", "-").
                                  replaceFirst("-", "").split("-")));
    }

    public ImageCash getImageCash() {
        return imageCash;
    }
    public void setImageCash(ImageCash imageCash) {
        this.imageCash = imageCash;
    }

    public CardExtract getCardExtract() {
        return cardExtract;
    }
    public void setCardExtract(CardExtract cardExtract) {
        this.cardExtract = cardExtract;
    }

    public HashMap<String, String[]> getDeck() {
        return deck;
    }
    public void setDeck(HashMap<String, String[]> deck) {
        this.deck = deck;
    }


    public static void main(String[] args) {
        //get sourceDir
        Path sourceDir;
        if (args.length == 0)
            sourceDir = Paths.get(System.getProperty("user.dir"));
        else
            sourceDir = Paths.get(args[0]);

        if (!Files.exists(sourceDir) || !Files.isDirectory(sourceDir)){
            LOGGER.log(Level.SEVERE, "{0} is not directory!", new Object[]{sourceDir});
            System.exit(0);
        }

        Main main = new Main();
        //fill searchList from deck
        ArrayList<ComparableDeck> searchList = new ArrayList<>();
        main.getDeck().forEach((key, cardHashes) ->
                Arrays.stream(cardHashes).forEach(cardHash -> searchList.add(new ComparableDeck(key, cardHash, 100))));

        //1. get all images from sourceDir
        //2. for each image extract cards as sub image
        //3. get pHash for each card
        //4. calculate distance between card pHash and each pHash in searchList
        //5. sort list on distance and get the first element
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.{png,jpg,gif}");
        try (Stream<Path> paths = Files.walk(sourceDir)) {
            paths.filter(path -> Files.isRegularFile(path) & matcher.matches(path.getFileName())).forEach(path -> {
                try {
                    BufferedImage img = ImageIO.read(new BufferedInputStream(Files.newInputStream(path)));

                    StringBuilder fileCardsCode = new StringBuilder(path.toString()).append("     ");
                    List<BufferedImage> cardList = main.getCardExtract().getCards(img);
                    for (BufferedImage card : cardList){
                        String cardHash = main.imageCash.getHash(card);

                        for (ComparableDeck compDeck : searchList){
                            compDeck.setDistance(main.getImageCash().distance(cardHash, compDeck.getCardHash()));
                        }
                        searchList.sort(Comparator.comparingInt(o -> o.distance));

                        if (searchList.size() == 0 || searchList.get(0).getDistance() > 5)
                            fileCardsCode.append("_unknown_card(").append(cardHash).append(")_");
                        else
                            fileCardsCode.append(searchList.get(0).cardName);
                    }
                    LOGGER.log(Level.INFO, fileCardsCode.toString());
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * searchList item
     */
    private static class ComparableDeck implements Comparable<Integer>{
        private String cardName;
        private String cardHash;
        private int distance;

        public ComparableDeck(String cardName, String cardHash, int distance){
            this.cardName = cardName;
            this.cardHash = cardHash;
            this.distance = distance;
        }

        public String getCardName() {
            return cardName;
        }
        public void setCardName(String cardName) {
            this.cardName = cardName;
        }

        public String getCardHash() {
            return cardHash;
        }
        public void setCardHash(String cardHash) {
            this.cardHash = cardHash;
        }

        public int getDistance() {
            return distance;
        }
        public void setDistance(int distance) {
            this.distance = distance;
        }

        @Override
        public int compareTo(Integer o) {
            return Integer.compare(distance, o);
        }
    }
}
