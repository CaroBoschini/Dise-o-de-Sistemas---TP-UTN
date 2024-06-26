package presentation;

import com.opencsv.CSVReader;

import example.hibernate.utils.BDUtils;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.UploadedFile;
import org.jetbrains.annotations.NotNull;
import presentation.dto.CargaMasivaDatos;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.*;
import java.util.Arrays;
import java.util.List;


public class CargaMasivaHandler implements Handler {
    @Override
    public void handle(@NotNull Context ctx) throws Exception {

        CargaMasivaDatos datos = ctx.bodyAsClass(CargaMasivaDatos.class);
            if (datos != null) {
                String response = "";
                List<String[]> csvData = datos.getDataAsList();
                switch (datos.getTipo()) {
                    case "EntidadescsvFile":
                        response = procesarEntidad_csv(csvData);
                        ctx.result("Procesando archivo EntidadescsvFile");
                        break;
                    case "OrganismoscsvFile":
                        response = procesarOrganismo_csv(csvData);
                        ctx.result("Procesando archivo OrganismoscsvFile");
                        break;
                }
                ctx.status(200).result(response);
            }
            else{
                ctx.status(400).result("No se ha cargado correctamente el csv.");
            }
        }

    public String procesarEntidad_csv(List<String[]> csvData) {
        String response = "No se han podido cargar las filas: ";

        EntityManager em = BDUtils.getEntityManager();
        BDUtils.comenzarTransaccion(em);

        try {

            for (int i = 0; i < csvData.size(); i++) {
                String[] row = csvData.get(i);

                if (row.length == 4) {
                    String sql = "INSERT INTO Entidad (id_entidad, nombre, tipo, id_localizacion) VALUES (?, ?, ?, ?)";

                    Query query = em.createNativeQuery(sql);

                    query.setParameter(1, Long.valueOf(row[0]));
                    query.setParameter(2, row[1]);
                    query.setParameter(3, row[2]);
                    query.setParameter(4, Long.valueOf(row[3]));
                    query.executeUpdate();
                }else{
                    response = response + i + " ";
                }
            }

            BDUtils.commit(em);
        } catch (Exception e) {
            BDUtils.rollback(em);
            e.printStackTrace();
            response = "";
        }
        em.close();
        return response;
    }
        public String procesarOrganismo_csv(List<String[]> csvData){return "";}

}
