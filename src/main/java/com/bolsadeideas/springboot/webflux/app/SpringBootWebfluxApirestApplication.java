package com.bolsadeideas.springboot.webflux.app;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.bolsadeideas.springboot.webflux.app.models.documents.Categoria;
import com.bolsadeideas.springboot.webflux.app.models.documents.Producto;
import com.bolsadeideas.springboot.webflux.app.models.service.ProductoService;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringBootWebfluxApirestApplication implements CommandLineRunner{
	@Autowired
	private ProductoService service ;
	
	@Autowired
	private ReactiveMongoTemplate monogTemplate;
	
	private static final Logger log = LoggerFactory.getLogger(SpringBootWebfluxApirestApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootWebfluxApirestApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		monogTemplate.dropCollection("productos").subscribe();
		monogTemplate.dropCollection("categorias").subscribe();
		
		Categoria electronico = new Categoria ("Electronico");
		Categoria deportes = new Categoria ("Deporte");
		Categoria computacion = new Categoria ("Computacion");
		Categoria muebles = new Categoria ("Muebles");
		
		Flux.just(electronico,deportes,computacion,muebles)
			.flatMap( service::saveCategoria)
			.doOnNext( c ->{
				log.info("Categoria Creada: "+c.getNombre() + " ,id: " + c.getId());
				
			}).thenMany(
					Flux.just(new Producto ("TV Panasonic Pantalla LCD",456.89,electronico),
							  new Producto ("Sony Camara HD Digital",456.89,electronico),
							  new Producto ("Apple Ipod",456.89,electronico),
							  new Producto ("Sony Notebook",456.89,computacion),
							  new Producto ("Hewlett Packard Multifuncional",456.89,computacion),
							  new Producto ("Bianchi Bicicleta",456.89,deportes),
							  new Producto ("HP Notebook Omen 17",456.89,computacion),
							  new Producto ("Mica Cómoda de 5 Cajones",456.89,muebles),
							  new Producto ("TV Sony Bravia OLED 4k Ultra HD",456.89,electronico))
					.flatMap(producto -> {
						producto.setCreateAt(new Date());
						return service.save(producto);
						
					})
					)
		.subscribe( producto -> log.info("insert :" + producto.getId() + " " +producto.getNombre()));
	}

}
