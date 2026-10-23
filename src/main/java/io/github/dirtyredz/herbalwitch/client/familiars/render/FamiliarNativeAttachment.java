package io.github.dirtyredz.herbalwitch.client.familiars.render;

/** Native renderer and headless pose qualification consume the same model-pixel anchors. */
public record FamiliarNativeAttachment(String bone,FamiliarEquipmentLayer.Attachment transform) {
    public static FamiliarNativeAttachment of(String species) {
        return switch(species) {
            case "cat" -> new FamiliarNativeAttachment("head",new FamiliarEquipmentLayer.Attachment(0,3,0,.4F));
            case "fox" -> new FamiliarNativeAttachment("body",new FamiliarEquipmentLayer.Attachment(4.3F,3,0,.45F,-90));
            case "wolf" -> new FamiliarNativeAttachment("upper_body",new FamiliarEquipmentLayer.Attachment(0,0,0,.85F,-90));
            case "rabbit" -> new FamiliarNativeAttachment("body",new FamiliarEquipmentLayer.Attachment(4.5F,0,-1,.35F));
            case "bat" -> new FamiliarNativeAttachment("body",new FamiliarEquipmentLayer.Attachment(0,7,-4,.45F));
            default -> throw new IllegalArgumentException("Unknown native familiar");
        };
    }
}
