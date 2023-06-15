package kyo.yaz.condominium.manager.ui.views.util;

import com.vaadin.flow.component.Component;

public class Labels {

    public static final String NEXT_PAGE = "Siguiente Página";
    public static final String PREVIOUS_PAGE = "Página Anterior";
    public static final String SAVE = "Guardar";
    public static final String UPDATE = "Actualizar";
    public static final String DELETE = "Borrar";
    public static final String CANCEL = "Cancelar";
    public static final String YEAR = "Año";
    public static final String MONTH = "Mes";
    public static final String EXPENSES = "Gastos";
    public static final String DEBTS = "Deudas";
    public static final String NEW_EXPENSE = "Nuevo Gasto";
    public static final String NEW_DEBT = "Nueva Deuda";
  public static final String ASK_CONFIRMATION_DELETE_TELEGRAM_CHAT = "Esta seguro de borrar el chat\n %s %s?";
    public static final String ASK_CONFIRMATION_DELETE_USERS = "Esta seguro de borrar el usuarios\n %s %s?";
    public static final String ASK_CONFIRMATION_DELETE_RATE = "Esta seguro de borrar la tasa de cambio\n %s %s %s?";
    public static final String ASK_CONFIRMATION_DELETE_APT = "Esta seguro de borrar el apartament \n %s %s?";
    public static final String ASK_CONFIRMATION_DELETE_BUILDING = "Esta seguro de borrar el edificio\n %s?";
    public static final String ASK_CONFIRMATION_DELETE_EMAIL_CONFIG = "Esta seguro de borrar la configuracioon de email\n %s?";
    public static final String COPY = "Copiar";
    public static final String ADD = "Añadir";
    public static final String EXTRA_CHARGE_TITLE = "Cargo Extra";
    public static final String REQUIRED = "Requerido";
    public static final String DEACTIVATED = "Desactivado";
    public static final String DOWNLOAD = "Descargar";
    public static final String SEND_EMAIL = "Enviar recibos";
    public static final String EDIT = "Editar";
    public static final String RESERVE_FUNDS_TITLE = "Fondos de reserva";
    public static final String VIEW_PDFS = "Ver PDFs";

  public static class Apartment {
        public static final String BUILDING_LABEL = "Edificio";
        public static final String NUMBER_LABEL = "Nro de apt";
        public static final String NAME_LABEL = "Propietario";
        public static final String ID_DOC_LABEL = "CI";
        public static final String EMAILS_LABEL = "Emails";
        public static final String PAYMENT_TYPE_LABEL = "Tipo de pago";
        public static final String AMOUNT_LABEL = "Monto";
        public static final String ALIQUOT_LABEL = "Aliquota";
    }

    public static class Building {

        public static final String ID_LABEL = "ID";

        public static final String NAME_LABEL = "Nombre";

        public static final String RIF_LABEL = "RIF";

        public static final String RESERVE_FUND_LABEL = "Fondo de reserva";

        public static final String RESERVE_FUND_CURRENCY_LABEL = "Fondo de reserva Moneda";

        public static final String MAIN_CURRENCY_LABEL = "Moneda Principal";
        public static final String DEBT_CURRENCY_LABEL = "Moneda Deudas";

        public static final String SHOW_PAYMENT_IN_CURRENCIES = "Monedas a mostrar al pagar";
        public static final String FIXED_PAY_LABEL = "Monto fijo";
        public static final String FIXED_PAY_AMOUNT_LABEL = "Monto fijo monto";
        public static final String FIXED_PAY_CURRENCY_LABEL = "Monto fijo moneda";
        public static final String RECEIPT_EMAIL_FROM_LABEL = "Email recibo";
        public static final String ROUND_UP_PAYMENTS_LABEL = "Redondear hacia arriba los montos a pagar";
        public static final String RESERVE_FUND_PERCENTAGE_LABEL = "Porcentage para el Fondo de reserva";
        public static final String AMOUNT_OF_APTS = "Cantidad de apartamentos";
    }


    public static class Receipt {
        public static final String VIEW_PAGE_TITLE = "Recibos de Pago";
        public static final String PDF_VIEW_PAGE_TITLE = "Recibo de Pago (PDF)";
        public static final String ADD_BUTTON_LABEL = "Recibo nuevo";
        public static final String AMOUNT_OF_LABEL = "Recibos: %d";
        public static final String ID_LABEL = "ID";
        public static final String BUILDING_LABEL = "Edificio";
        public static final String DATE_LABEL = "Fecha";
        public static final String EXPENSE_COMMON_LABEL = "Gastos comunes";
        public static final String EXPENSE_UNCOMMON_LABEL = "Gastos no comunes";
        public static final String DEBT_RECEIPT_TOTAL_NUMBER_LABEL = "Recibos";
        public static final String DEBT_RECEIPT_TOTAL_AMOUNT_LABEL = "Total de deuda";
        public static final String CREATED_AT_LABEL = "Fecha de creación";
        public static final String RECEIPT_DATE_LABEL = "Fecha de recibo";
        public static final String RATE_LABEL = "Tasa";
        public static final String ASK_CONFIRMATION_DELETE = "Esta seguro de borrar el recibo\n %s %s %s?";
        public static final String YEAR_LABEL = "Año";
        public static final String MONTH_LABEL = "Mes";
        public static final String SENT_LABEL = "Enviado";
    }

    public static class Expense {
        public static final String DESCRIPTION_LABEL = "Descripcion";
        public static final String AMOUNT_LABEL = "Monto";
        public static final String TYPE_LABEL = "Tipo";
        public static final String CURRENCY_LABEL = "Moneda";
    }

    public static class Debt {

        public static final String APT_NUMBER_LABEL = "Nro. de apartamento";
        public static final String APT_LABEL = "Apartamento";

        public static final String NAME_LABEL = "Nombre";

        public static final String RECEIPT_LABEL = "Recibos";

        public static final String AMOUNT_LABEL = "Monto";
        public static final String CURRENCY_LABEL = "Moneda";

        public static final String MONTHS_LABEL = "Meses";

        public static final String PREVIOUS_AMOUNT_PAYED_LABEL = "Abono monto";
        public static final String PREVIOUS_AMOUNT_CURRENCY_PAYED_LABEL = "Abono moneda";
    }

    public static class Rate {
        public static final String ID_LABEL = "ID";
        public static final String RATE_LABEL = "Tasa";
        public static final String ROUNDED_RATE_LABEL = "Tasa Redondeada";
        public static final String DATE_OF_RATE_LABEL = "Fecha de la tasa";
        public static final String SOURCE_LABEL = "Fuente";
        public static final String CURRENCIES_LABEL = "Monedas";
        public static final String CREATED_AT_LABEL = "Creado en";


    }

    public static class ExtraCharge {
        public static final String APT_LABEL = "Apartamento";
        public static final String DESCRIPTION_LABEL = "Descripcion";
        public static final String AMOUNT_LABEL = "Monto";
        public static final String CURRENCY_LABEL = "Moneda";

    }

    public static class ReserveFund {
        public static final String NAME_LABEL = "Nombre";
        public static final String FUND_LABEL = "Fondo";
        public static final String CURRENCY_LABEL = "Moneda";
        public static final String PAY_LABEL = "Monto a pagar";
        public static final String ACTIVE_LABEL = "Activado";
        public static final String TYPE_LABEL = "Tipo";
        public static final String EXPENSE_TYPE_LABEL = "Tipo de gasto";
        public static final String ADD_TO_EXPENSES_LABEL = "Añadir a gastos";
    }

    public static class EmailConfig {
        public static final String ID_LABEL = "ID";
        public static final String FROM_LABEL = "Email";
        public static final String ACTIVE_LABEL = "Activado";
        public static final String CONFIGURATION_LABEL = "Configuración";
        public static final String STORED_CREDENTIAL_LABEL = "Credential";
        public static final String IS_AVAILABLE_LABEL = "Disponible";
        public static final String CREATED_AT_LABEL = "Fecha de creación";
        public static final String UPDATED_AT_LABEL = "Actualizado en";
        public static final String LAST_CHECK_AT_LABEL = "Ultimo chequeo";
    }

    public static class TelegramChat {

      public static final String NOTIFICATION_LABEL = "Notificaciones";
    }
}
