import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.javalin.http.servlet.JavalinServletContext;
import io.javalin.openapi.plugin.OpenApiConfiguration;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import io.javalin.rendering.JavalinRenderer;
import org.quartz.*;
import presentation.*;

import persistance.Programador;
import presentation.CargaMasivaHandler;
import presentation.controller.*;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.IOException;


public class Application {
    public static void main(String[] args) throws SchedulerException {
        // Ruta al archivo JSON de configuración descargado
        String pathToFirebaseConfig = "path/to/your/firebase-config.json";
/*
        try {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(new FileInputStream(pathToFirebaseConfig)))
                    .setDatabaseUrl("https://your-firebase-database-url.firebaseio.com%22/)
                            .build();

            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            e.printStackTrace();
        }*/


        //Inicializo el motor de templates
        initTemplateEngine();

        Javalin app = Javalin.create(config -> {
                    config.plugins.register(new OpenApiPlugin(new OpenApiConfiguration()));
                    config.plugins.register(new SwaggerPlugin(new SwaggerConfiguration()));
                    config.plugins.enableCors(cors -> {
                        cors.add(it-> it.anyHost());
                    });
                    config.staticFiles.add("/public");
                }).start(4567);

        app.get("/", ctx -> {
            ctx.attribute("key", "value");
            ctx.redirect("/login.html");
        });


        System.out.println("Check out Swagger UI docs at http://localhost:4567/swagger");


        app.get("/api/perfil/{id}",     new GetPerfilHandler());
        app.get("/api/comunidad/{id}",  new GetComunidadHandler());
        app.get("api/Entidades", new GetEntidadesHandler());
        app.get("api/Establecimientos/{id}", new GetEstablecimientosPorEntidadHandler());
        app.get("api/Servicios/{id}",new GetServiciosPorEstablecimiento());


        app.get("/cargaMasivaLiviano", ctx ->{
           ctx.redirect("/cargaMasiva.html");
        });
        app.post("/cargaMasivaLiviano/cargar", new CargaMasivaHandler());


        app.get("/administrarUsuarioLiviano", new administrarUsuarioController());
        app.post("/administrarUsuarioLivianoAplicar", new administrarUsuarioHandler()); //Ni yo me acuerdo que hice aca



        app.post("/cookies/incidentesLiviano/comunidad", ctx -> {
            ctx.cookie("COMUNIDAD_INCIDENTE",ctx.body());
        });
        app.post("/cookies/incidentesLiviano/filtro", ctx -> {
            ctx.cookie("FILTRO_INCIDENTE",ctx.body());
        });
        app.get("/incidentesLiviano", new incidentesPorEstadoController());
        app.get("/incidentesLiviano/tabla", new incidentesPorEstadoTablaController());


        app.get("/rankingsLiviano", new RankingsController()); //PONELE QUE ESTA (HAY QUE HACER LA PRUEBA CON DB)

        app.post("/api/login", new LoginHandler());


        Programador.programar();
    }

    private static void initTemplateEngine() {
        //Setea el motor de Templates para renderizar los archivo de texto

        JavalinRenderer.register(
                (path, model, context) -> { // Función que renderiza el template
                    Handlebars handlebars = new Handlebars();
                    Template template = null;
                    try {
                        template = handlebars.compile(
                                "public/" + path.replace(".hbs", ""));
                        return template.apply(model);
                    } catch (IOException e) {
                        e.printStackTrace();
                        context.status(HttpStatus.NOT_FOUND);
                        return "No se encuentra la página indicada...";
                    }
                }, ".hbs" // Extensión del archivo de template
        );
    }


}
/*
 INSERT INTO `prueba_api`.`perfil`
        (`id_perfil`,
        `confianza`,
        `puntaje`)
        VALUES
        (1,
        'ConfiableNivel1',
        6.0),
        (4,
        'NoConfiable',
        2.0),
        (6,
        'ConfiableNivel2',
        9.0);

INSERT INTO `prueba_api`.`comunidad`
        (`id_comunidad`,
        `confianza`,
        `puntaje`)
        VALUES
        (5,
        'ConfiableNivel1',
        5.0),
        (2,
        'ConfiableNivel2',
        5.0),
        (3,
        'NoConfiable',
        5.0);


INSERT INTO `prueba_api`.`comunidad_x_perfil` (`comunidad_id`, `perfil_id`)
SELECT DISTINCT id_comunidad, id_perfil
FROM (
	SELECT id_comunidad , id_perfil
	FROM comunidad
	CROSS JOIN perfil
	WHERE (id_comunidad, id_perfil) NOT IN (
		SELECT comunidad_id, perfil_id
		FROM `comunidad_x_perfil`
	)
	ORDER BY RAND()
	LIMIT 1
) AS tmp;

INSERT INTO `prueba_api`.`incidente`
	(`id_incidente`, `apertura`, `cierre`, `observaciones`, `id_perfil_apertura`, `id_perfil_cierre`)
VALUES
	(ROUND(RAND() * 99 + 1), -- id_incidente aleatorio entre 1 y 100
	 NOW() - INTERVAL FLOOR(RAND() * 7) DAY, -- Fecha de apertura aleatoria en los últimos 30 días
	 NOW() - INTERVAL FLOOR(RAND() * 2) DAY, -- Fecha de cierre aleatoria en los últimos 10 días
	 'Observaciones aleatorias', -- Observaciones fijas o aleatorias según lo desees
	 FLOOR(RAND() * 5 + 1), -- id_perfil_apertura aleatorio entre 1 y 5
	 FLOOR(RAND() * 5 + 1) -- id_perfil_cierre aleatorio entre 1 y 5
	);

*/
