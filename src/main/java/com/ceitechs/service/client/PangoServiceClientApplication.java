package com.ceitechs.service.client;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@EnableHystrix
@EnableDiscoveryClient
@EnableFeignClients
@EnableZuulProxy
@SpringBootApplication
public class PangoServiceClientApplication {

    @Bean
    CommandLineRunner runner (DiscoveryClient discoveryClient){
        return  args ->{
            discoveryClient.getInstances("reservation-service").forEach(i -> System.out.println(
                    i.getServiceId() + ' '
                    + i.getHost() + ' '
                    + i.getPort()

            ));
        };
    }


    @Bean
    CommandLineRunner feignCleints(ReservationClient reservationClient){
        return args -> {
            //ParameterizedTypeReference<List<Reservation>> listParameterizedTypeReference = new ParameterizedTypeReference<List<Reservation>>() { };
            reservationClient.findReservation().forEach(System.out::println);
        };
    }

	public static void main(String[] args) {
		SpringApplication.run(PangoServiceClientApplication.class, args);
	}
}

@Getter
@Setter
class Reservation {

    private Long id;

    private String reservationName;

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", reservationName='" + reservationName + '\'' +
                '}';
    }
}

@RestController
class RestClientController{
    @Autowired
    private  ReservationIntegration reservationIntegration;

    @RequestMapping("/custom-reservations")
    public Collection<Reservation> reservations() {
        return this.reservationIntegration.getReservation();
    }
}



@FeignClient(value = "reservation-service", fallback = ReservationClientImpl.class)
interface ReservationClient{
    @RequestMapping(value = "/reservations", method = RequestMethod.GET)
    Collection<Reservation> findReservation();
}
@Service
class ReservationClientImpl implements ReservationClient{

    @Override
    public Collection<Reservation> findReservation() {
        return  Collections.emptyList();
    }
}

@Service
class ReservationIntegration {

    @Autowired
    private ReservationClient reservationClient;



   // @HystrixCommand(fallbackMethod = "getReservationsFallback")
    Collection<Reservation> getReservation(){
        return reservationClient.findReservation();
    }

}
