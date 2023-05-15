package kyo.yaz.condominium.manager.ui.views.actions;

public interface DownloadReceiptZipAction<T> {

    void downloadBtnClicked();

    String fileName(T obj);

    String filePath(T obj);

    void downloadFinished(Runnable runnable);
}
