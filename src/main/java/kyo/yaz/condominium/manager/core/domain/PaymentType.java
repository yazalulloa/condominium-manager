package kyo.yaz.condominium.manager.core.domain;

import kyo.yaz.condominium.manager.persistence.entity.Apartment;

public enum PaymentType {
    ALIQUOT, FIXED_PAY;

    public static final PaymentType[] values = values();
}
