package com.albatros.simspriser.rest.controller;

import com.albatros.simspriser.domain.DiraUser;
import com.albatros.simspriser.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@RequestMapping("/user")
@RestController
@RequiredArgsConstructor
public class UserController {

    @Autowired
    private final UserService service;

    @PostMapping(value = "/create", consumes = "application/json", produces = "application/json")
    public DiraUser createUser(@RequestBody DiraUser user) throws ExecutionException, InterruptedException {
        service.saveUser(user);
        return user;
    }

    @GetMapping(value = "/league/get/all")
    public List<DiraUser> getUsersByLeague(@RequestParam("league") int league) throws ExecutionException, InterruptedException {
        return service.getUsers().stream().filter(u -> u.getLeague() == league).collect(Collectors.toList());
    }

    @GetMapping(value = "/refresh/leagues")
    public void refreshLeagues() throws ExecutionException, InterruptedException {
        List<DiraUser> users = service.getUsers();

        Consumer<DiraUser> leagueIncreaseConsumer = u -> {
            u.setLeague(u.getLeague() + 1);
            try {
                service.saveUser(u);
            } catch (ExecutionException | InterruptedException ignored) {
            }
        };

        int i = 4;
        while (i > 0) {
            int finalI = i;
            users.stream()
                    .filter(u -> u.getLeague() == finalI)
                    .sorted(Comparator.comparingInt(DiraUser::getScoreOfWeek).reversed())
                    .limit(1)
                    .forEach(leagueIncreaseConsumer);
            i--;
        }
    }

    @GetMapping(value = "/refresh/day")
    public void refreshDay() throws ExecutionException, InterruptedException {
        List<DiraUser> users = service.getUsers();
        for (DiraUser user : users) {
            user.setScoreOfDay(0);
            service.saveUser(user);
        }
    }

    @GetMapping(value = "/refresh/week")
    public void refreshWeek() throws ExecutionException, InterruptedException {
        List<DiraUser> users = service.getUsers();
        for (DiraUser user : users) {
            user.setScoreOfWeek(0);
            service.saveUser(user);
        }
    }

    @GetMapping("/find")
    public ResponseEntity<DiraUser> findById(@RequestParam("user_id") String user_id) throws ExecutionException, InterruptedException {
        for (DiraUser user : service.getUsers()) {
            if (user.getTokenId().equalsIgnoreCase(user_id)) {
                return ResponseEntity.ok(user);
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/get/all")
    public List<DiraUser> getAll() throws ExecutionException, InterruptedException {
        return service.getUsers();
    }
}
