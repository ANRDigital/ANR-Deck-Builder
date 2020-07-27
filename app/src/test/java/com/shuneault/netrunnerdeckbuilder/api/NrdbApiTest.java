package com.shuneault.netrunnerdeckbuilder.api;

import org.junit.Test;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class NrdbApiTest {
    @Test
    public void PublicDeckListByDate_ReturnsData() throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://netrunnerdb.com/api/2.0/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        NrdbApiService service = retrofit.create(NrdbApiService.class);

        Call<NrdbDeckLists> call = service.getDateDeckLists("2020-07-19");
        Response<NrdbDeckLists> response = call.execute();
        if (response.isSuccessful()){

            List<NrdbDeckList> data = response.body().getData();
            assertTrue(data.size() > 0);
            assertTrue(data.get(0).getCardCounts().size() > 0);
        }
        else
            fail();
    }
}
