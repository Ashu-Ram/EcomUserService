package dev.ashu.userservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ashu.userservice.config.KafkaProducerConfig;
import dev.ashu.userservice.dto.SendEmailDto;
import dev.ashu.userservice.dto.UserDto;
import dev.ashu.userservice.exception.*;
import dev.ashu.userservice.mapper.UserEntityDTOMapper;
import dev.ashu.userservice.model.Session;
import dev.ashu.userservice.model.SessionStatus;
import dev.ashu.userservice.model.User;
import dev.ashu.userservice.repository.SessionRepository;
import dev.ashu.userservice.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.MacAlgorithm;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMapAdapter;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class AuthService {
    private final KafkaProducerConfig kafkaProducerConfig;

    private final UserRepository userRepository;

    private final SessionRepository sessionRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final ObjectMapper objectMapper;
    public AuthService(KafkaProducerConfig kafkaProducerConfig, UserRepository userRepository, SessionRepository sessionRepository, BCryptPasswordEncoder bCryptPasswordEncoder, ObjectMapper objectMapper) {
        this.kafkaProducerConfig = kafkaProducerConfig;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.objectMapper = objectMapper;

    }

    public ResponseEntity<UserDto> login(String email, String password) {
        //Get User details from dB
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("User for given email id does not exist");
        }

        User user = userOptional.get();
        // Verify user password given at the time of login
        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialException("Invalid Credentials");
        }

        //  String token = RandomStringUtils.randomAlphanumeric(30);
        MacAlgorithm alg = Jwts.SIG.HS256;//HS256 algo added for JWT
        SecretKey key = alg.key().build();

        //start adding the claim
        Map<String, Object> jsonForJWT = new HashMap<>();
        // jsonForJWT.put("email",user.getEmail());
        jsonForJWT.put("userId", user.getId());
        jsonForJWT.put("roles", user.getRoles());
        jsonForJWT.put("createdAt", new Date());
        jsonForJWT.put("expiryAt", new Date(LocalDate.now().plusDays(3).toEpochDay()));


        String token = Jwts.builder()
                .claims(jsonForJWT) // added the claims
                .signWith(key, alg) // added the algo and key
                .compact(); // building the token

        // Session Creation
        Session session = new Session();
        session.setSessionStatus(SessionStatus.ACTIVE);
        session.setToken(token);
        session.setUser(user);
        session.setLoginAt(new Date());
        sessionRepository.save(session);
        // Generating the response
        UserDto userDto = UserEntityDTOMapper.getUserDTOFromUserEntity(user);
        // MultiValueMapAdapter is map with  single key and mulitple values

        //Setting up the headers
        MultiValueMapAdapter<String, String> headers = new MultiValueMapAdapter<>(new HashMap<>());
        headers.add(HttpHeaders.SET_COOKIE, token);

        return new ResponseEntity<>(userDto, headers, HttpStatus.OK);
    }


//    public UserDto signUp(String email, String password) {
//        User user = new User();
//        user.setEmail(email);
//        user.setPassword(bCryptPasswordEncoder.encode(password));
//        User savedUser = userRepository.save(user);
//        return UserDto.from(savedUser);
//    }

    public UserDto signUp(String email, String password) {
        // 1️⃣ Check if email already exists
        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException("Email is already registered.");
        }

        // 2️⃣ Create new user and encrypt password
        User user = new User();
        user.setEmail(email);
        user.setPassword(bCryptPasswordEncoder.encode(password));

        // 3️⃣ Save user to the database
        User savedUser = userRepository.save(user);
        //if a user has signed up successfully then push an event inside the queue with a particular topic.
//       try {
//           SendEmailDto sendEmailDto = new SendEmailDto();
//           sendEmailDto.setTo(savedUser.getEmail());
//           sendEmailDto.setSubject("SignUp Successfull");
//           sendEmailDto.setBody("Welcome to Scaler !");
//           sendEmailDto.setFrom("thakurashu653@gmail.com");
//           kafkaProducerConfig.sendMessage("signUp",objectMapper. writeValueAsString(sendEmailDto));
//       } catch (Exception e) {
//           System.out.println("Something went wrong");
//       }


        // 4️⃣ Convert entity to DTO and return response
        return UserDto.from(savedUser);
    }


//    public ResponseEntity<Void> logout(String token, Long userId) {
//        // validations -> token exists, token is not expired, user exists else throw an exception
//        Optional<Session> sessionOptional = sessionRepository.findByTokenAndUser_Id(token, userId);
//        if (sessionOptional.isEmpty()) {
//            return null;//TODO  throw exception here
//        }
//        Session session = sessionOptional.get();
//        session.setSessionStatus(SessionStatus.ENDED);
//        sessionRepository.save(session);
//        return ResponseEntity.ok().build();
//    }

    public ResponseEntity<Map<String, String>> logout(String token, Long userId) {
        // 1️⃣ Check if the user exists
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }

        // 2️⃣ Check if the token exists
        Optional<Session> sessionOptional = sessionRepository.findByTokenAndUser_Id(token, userId);
        if (sessionOptional.isEmpty()) {
            throw new InvalidTokenException("Invalid token or session does not exist");
        }

        // 3️⃣ Check if the token is expired
        Session session = sessionOptional.get();
        if (session.getSessionStatus().equals(SessionStatus.ENDED)) {
            throw new ExpiredTokenException("Token is already expired");
        }

        // 🔄 Update session status to ENDED
        session.setSessionStatus(SessionStatus.ENDED);
        sessionRepository.save(session);

        // ✅ Return success response
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout successful");
        return ResponseEntity.ok(response);
    }

    public SessionStatus validate(String token, Long userId) {
        //TODO check expiry // Jwts Parser -> parse the encoded JWT token to read the claims


        //verifying from DB if session exists
        Optional<Session> sessionOptional = sessionRepository.findByTokenAndUser_Id(token, userId);

        if (sessionOptional.isEmpty() || sessionOptional.get().getSessionStatus().equals(SessionStatus.ENDED)) {
            throw new InvalidTokenException("token is invalid || ENDED");
        }
        return SessionStatus.ACTIVE;
    }

//Below APIS are not part of project ,used for personal use

    public ResponseEntity<List<Session>> getAllSession() {
        List<Session> sessions = sessionRepository.findAll();
        return ResponseEntity.ok(sessions);
    }

    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

}
