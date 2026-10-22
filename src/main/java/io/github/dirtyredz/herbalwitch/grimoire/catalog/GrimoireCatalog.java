package io.github.dirtyredz.herbalwitch.grimoire.catalog;

import java.util.List;

/**
 * The grimoire's permanent identities (docs/catalogue/stations.md): the portable book, the two
 * research blocks and the reader menu. Plain data, no registry access; both research blocks share
 * one behaviour and differ only in their entry here and their art. Ids never change once a world
 * uses them.
 */
public final class GrimoireCatalog {
    /** The page a reader opens on. */
    public enum Opens {
        /** The full index (search, categories). */
        INDEX,
        /** The research filter: recipes not yet known whose ingredients are partly familiar. */
        RESEARCH
    }

    /**
     * One research block.
     *
     * @param directory   id segment and model/texture directory
     * @param displayName English name
     * @param opens       the page its reader opens on
     */
    public record ResearchPiece(String directory, String displayName, Opens opens) {
        /** {@code grimoire/<directory>}. */
        public String path() {
            return "grimoire/" + directory;
        }
    }

    public static final String BOOK_PATH = "grimoire/book";
    public static final String BOOK_NAME = "Grimoire";
    public static final String READER_PATH = "grimoire/reader";

    public static final ResearchPiece STAND = new ResearchPiece("stand", "Grimoire Stand", Opens.RESEARCH);
    public static final ResearchPiece BOOKSHELF = new ResearchPiece("bookshelf", "Witch's Bookshelf", Opens.INDEX);

    /** Creative-tab and showcase order. */
    public static final List<ResearchPiece> RESEARCH_PIECES = List.of(STAND, BOOKSHELF);

    private GrimoireCatalog() {
    }
}
