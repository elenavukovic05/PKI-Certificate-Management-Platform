package pki_project.pki.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pki_project.pki.dto.ChangePasswordDTO;
import pki_project.pki.dto.UserDTO;
import pki_project.pki.model.enums.Role;
import pki_project.pki.services.UserService;

import java.util.List;

@RestController
@RequestMapping(value = "api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/regular")
    public List<UserDTO> getUsers() {
        return userService.getRegularUsers() ;
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody  ChangePasswordDTO request){
        try{
            userService.changePassword(request);
            return ResponseEntity.ok("Password changed successfully");
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

}
