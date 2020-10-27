package ch.so.agi.sodata.client;

import org.dominokit.domino.ui.cards.Card;
import org.dominokit.domino.ui.utils.BaseDominoElement;
import elemental2.dom.HTMLDivElement;

public class MapCard extends BaseDominoElement<HTMLDivElement, MapCard> {

    private Card mapCard;

    public MapCard(String mapId) {
        this(mapId, null);
    }

    public MapCard(String mapId, String title) {

        this.mapCard = new Card();

        if (title != null) {
            this.mapCard.setTitle(title);
        }

        init(this);
        this.mapCard.fitContent();
        this.mapCard.getBody().setId(mapId);
    }

    @Override
    public HTMLDivElement element() {
        return this.mapCard.element();
    }

    public final Card getCard() {
        return this.mapCard;
    }
}

