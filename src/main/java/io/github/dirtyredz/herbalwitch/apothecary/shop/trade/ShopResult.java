package io.github.dirtyredz.herbalwitch.apothecary.shop.trade;

import java.util.UUID;

/**
 * The answer to one commerce request: success or a refusal code (a translation suffix under
 * {@code shop.herbalwitch.result.}), plus the transaction identity when one was issued, so an
 * unresolved transfer is never shown as success.
 */
public record ShopResult(boolean success, String code, UUID transaction) {
    public static final String PREFIX = "shop.herbalwitch.result.";

    public static ShopResult ok(String code) {
        return new ShopResult(true, code, null);
    }

    public static ShopResult ok(String code, UUID transaction) {
        return new ShopResult(true, code, transaction);
    }

    public static ShopResult refused(String code) {
        return new ShopResult(false, code, null);
    }

    public static ShopResult recovery(String code, UUID transaction) {
        return new ShopResult(false, code, transaction);
    }

    public String key() {
        return PREFIX + code;
    }
}
