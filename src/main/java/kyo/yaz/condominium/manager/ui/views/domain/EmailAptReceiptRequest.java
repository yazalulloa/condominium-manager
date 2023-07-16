package kyo.yaz.condominium.manager.ui.views.domain;


import kyo.yaz.condominium.manager.persistence.entity.Apartment;
import kyo.yaz.condominium.manager.persistence.entity.Building;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;

import java.util.List;

public record EmailAptReceiptRequest(
        String subject, String message,
        Receipt receipt, Building building, List<Apartment> apartments) {

}
