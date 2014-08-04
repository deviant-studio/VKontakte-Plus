package ds.vkplus;

import dagger.Module;
import dagger.Provides;
import ds.vkplus.network.RestService;

import javax.inject.Singleton;

@Module(library = true,
		injects = {

		}
)
public class DaggerModule {

	@Provides
	@Singleton
	RestService provideRestService() {
		return new RestService();
	}
}
