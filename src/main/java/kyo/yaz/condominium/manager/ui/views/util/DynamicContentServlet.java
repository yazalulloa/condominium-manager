package kyo.yaz.condominium.manager.ui.views.util;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;
import java.nio.file.Files;

@WebServlet(urlPatterns = "/image", name = "DynamicContentServlet", asyncSupported = true)
public class DynamicContentServlet extends HttpServlet {
    private static final int ARBITARY_SIZE = 2048;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        final var file = new File("tmp" + "/" + req.getParameter("f"));

        if (!file.exists()) {
            resp.sendError(404, "FILE NOT FOUND");
            return;
        }


        resp.setContentType(Files.probeContentType(file.toPath()));

        resp.setHeader("Content-disposition", "attachment; filename=" + file.getName());

        try (InputStream in = new FileInputStream(file);
             OutputStream out = resp.getOutputStream()) {

            byte[] buffer = new byte[ARBITARY_SIZE];

            int numBytesRead;
            while ((numBytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, numBytesRead);
            }
        }
    }
}
