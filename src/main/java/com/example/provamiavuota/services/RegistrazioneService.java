package com.example.provamiavuota.services;

import com.example.provamiavuota.dto.LoginDTO;
import com.example.provamiavuota.dto.UtenteRegistrDTO;
import com.example.provamiavuota.entities.Carrello;
import com.example.provamiavuota.entities.Utente;
import com.example.provamiavuota.repositories.CarrelloRepository;
import com.example.provamiavuota.repositories.UtenteRepository;
import com.example.provamiavuota.supports.ResponseMessage;
import com.example.provamiavuota.supports.exceptions.ErroreLoginException;
import com.example.provamiavuota.supports.exceptions.ErroreLogoutException;
import com.example.provamiavuota.supports.exceptions.ErroreNellaRegistrazioneUtenteException;
import jakarta.ws.rs.core.Response;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.*;

@Service
public class RegistrazioneService {

    @Autowired
    private UtenteRepository utenteRepository;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.auth-server-url}")
    private String serverUrl;

    @Value("${keycloak.clientId}")
    private String clientId;
    @Value("${keycloak.credentials.secret}")
    private String secret;
    private String usernameAdmin = "admin";
    private String passwordAdmin = "admin";
    @Autowired
    private CarrelloRepository carrelloRepository;


    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public ResponseEntity registraNuovoUtente(UtenteRegistrDTO user) throws ErroreNellaRegistrazioneUtenteException {
        if (user == null) {
            throw new ErroreNellaRegistrazioneUtenteException();
        }

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

        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setValue(user.password());
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);

        List<CredentialRepresentation> list = new ArrayList<>();
        list.add(credentialRepresentation);

        userk.setCredentials(list);

        userk.setAttributes(Collections.singletonMap("origin", Arrays.asList("demo")));

        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersRessource = realmResource.users();

        Response response = usersRessource.create(userk);
        if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
            String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

            ClientRepresentation clientRep = realmResource.clients().findByClientId(clientId).get(0);
            ClientResource clientResource = realmResource.clients().get(clientRep.getId());

            RoleRepresentation userRole = clientResource.roles().get("utente").toRepresentation();
            usersRessource.get(userId).roles().clientLevel(clientResource.toRepresentation().getId()).add(Collections.singletonList(userRole));

            //SALVO NEL DB IL MIO UTENTE
            Utente u = new Utente();
            u.setPuntifedelta(0);
            u.setOrdini(new ArrayList<>());
            u.setNome(user.firstName());
            u.setCognome(user.lastName());
            u.setEmail(user.email());
            Carrello c = new Carrello();
            c.setUtente(u);
            c.setAttivo(1);
            c.setListaDettagliCarrello(new LinkedList<>());
            utenteRepository.save(u);
            carrelloRepository.save(c);
            return new ResponseEntity(user, HttpStatus.OK);

        } else {
            throw new ErroreNellaRegistrazioneUtenteException();
        }
    }


    public ResponseEntity loginUser(LoginDTO loginDTO) throws ErroreLoginException {
        if (loginDTO == null) {
            throw new ErroreLoginException();
        }
        Keycloak keycloakLogin = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(secret)
                .username(loginDTO.username())
                .password(loginDTO.password())
                .grantType(OAuth2Constants.PASSWORD)
                .build();

        AccessTokenResponse tokenResponse = keycloakLogin.tokenManager().grantToken();
        if (tokenResponse == null || tokenResponse.getToken() == null || tokenResponse.getToken().isEmpty()) {
            throw new ErroreLoginException();
        }

        return new ResponseEntity<>(tokenResponse, HttpStatus.OK);

    }

    public ResponseEntity logoutUser(String refreshToken) throws ErroreLogoutException {
        try {
            Keycloak keycloak = KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm(realm)
                    .clientId(clientId)
                    .clientSecret(secret)
                    .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                    .build();

            keycloak.tokenManager().invalidate(refreshToken);

            return new ResponseEntity<>(new ResponseMessage("LOGOUT EFFETTUATO CON SUCCESSO"), HttpStatus.OK);
        } catch (Exception e) {
            throw new ErroreLogoutException();
        }
    }
}
