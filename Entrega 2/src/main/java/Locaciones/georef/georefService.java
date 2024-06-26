package Locaciones.georef;

import Locaciones.ListadoMunicipio;
import Locaciones.ListadoProvincias;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface georefService {
    @GET("provincias")
    Call<ListadoProvincias> provincias();

    @GET("provincias")
    Call<ListadoProvincias> provincias(@Query("campos") String campos);

    @GET("municipios")
    Call<ListadoMunicipio> municipios(@Query("provincia") int idProvincia);

    @GET("municipios")
    Call<ListadoMunicipio> municipios(@Query("provincia") int idProvincia, @Query("campos") String campos);

    @GET("municipios")
    Call<ListadoMunicipio> municipios(@Query("provincia") int idProvincia, @Query("campos") String campos, @Query("max") int max);
}