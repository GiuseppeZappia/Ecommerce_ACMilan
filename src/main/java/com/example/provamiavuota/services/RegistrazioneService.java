package com.example.provamiavuota.services;

import com.example.provamiavuota.authentication.Utils;
import com.example.provamiavuota.dto.UtenteRegistrDTO;
import com.example.provamiavuota.supports.ResponseMessage;
import com.example.provamiavuota.supports.exceptions.ErroreLoginException;
import com.example.provamiavuota.supports.exceptions.ErroreNellaRegistrazioneUtenteException;
import jakarta.ws.rs.core.Response;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.management.relation.Role;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class RegistrazioneService {

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.auth-server-url}")
    private String serverUrl;

    @Value("${keycloak.clientId}")
    private String clientId;
    @Value("${keycloak.credentials.secret}")
    private String secret;
    private String usernameAdmin="admin";
    private String passwordAdmin="admin";

    public ResponseEntity registraNuovoUtente(@RequestBody UtenteRegistrDTO user) throws ErroreNellaRegistrazioneUtenteException {
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType(OAuth2Constants.PASSWORD)
                .clientId(clientId)
                .clientSecret(secret)
                .username(usernameAdmin)
                .password(passwordAdmin)
                .build();

        UserRepresentation userk = new UserRepresentation();


        userk.setEnabled(true);
        userk.setUsername(user.username());
        userk.setEmail(user.email());
        userk.setFirstName(user.firstName());
        userk.setLastName(user.lastName());
        userk.setEmailVerified(true);

        CredentialRepresentation credentialRepresentation=new CredentialRepresentation();
        credentialRepresentation.setValue(user.password());
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);

        List<CredentialRepresentation> list= new ArrayList<>();
        list.add(credentialRepresentation);

        userk.setCredentials(list);

        userk.setAttributes(Collections.singletonMap("origin", Arrays.asList("demo")));

        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersRessource = realmResource.users();

        Response response = usersRessource.create(userk);
        if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
            // Retrieve the ID of the newly created user
            String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

            ClientRepresentation clientRep = realmResource.clients().findByClientId(clientId).get(0);
            ClientResource clientResource = realmResource.clients().get(clientRep.getId());

            RoleRepresentation userRole = clientResource.roles().get("utente").toRepresentation();
            usersRessource.get(userId).roles().clientLevel(clientResource.toRepresentation().getId()).add(Collections.singletonList(userRole));

            return new ResponseEntity(user, HttpStatus.OK);

        }
        else {
            throw new ErroreNellaRegistrazioneUtenteException();
        }
    }

    private UsersResource getUsersResource()  {
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType(OAuth2Constants.PASSWORD)
                .clientId(clientId)
                .clientSecret(secret)
                .username(usernameAdmin)
                .password(passwordAdmin)
                .build();

        RealmResource realm1=keycloak.realm(realm);
        return realm1.users();
    }

    public UserRepresentation getUserById(String userId){// throws ErroreLoginException {
//        try{

        System.out.println(getUsersResource().get(userId));
        return getUsersResource().get(Utils.getEmail()).toRepresentation();
//        }catch (Exception e){
//            throw new ErroreLoginException();
//        }
    }


}
