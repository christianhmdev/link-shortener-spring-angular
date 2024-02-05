package com.linkshortener.service;

import com.linkshortener.entity.Link;
import com.linkshortener.entity.User;
import com.linkshortener.exception.LinkAlreadyExistException;
import com.linkshortener.exception.LinkNotFoundException;
import com.linkshortener.repository.LinkRepository;
import com.linkshortener.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class LinkService {
    private final static Logger LOGGER = LoggerFactory.getLogger(LinkService.class);
    private final LinkRepository linkRepository;
    private final UserRepository userRepository;

    public LinkService(LinkRepository linkRepository, UserRepository userRepository) {
        this.linkRepository = linkRepository;
        this.userRepository = userRepository;
    }

    public Optional<Link> getLinkById(long id) {
        Optional<Link> link = linkRepository.findById(id);
        Optional<User> user = getUserFromAuthContext();

        if (isUsersLink(user, link)) {
            return link;
        }

        return Optional.empty();
    }

    public Link getUsersLinkByAlias(String alias) {
        Optional<Link> link = getUsersLink(alias);

        if (link.isPresent()) {
            return link.get();
        }

        throw new LinkNotFoundException(alias);
    }

    public List<Link> getAllLinks() {
        Optional<User> optionalUser = getUserFromAuthContext();
        List<Link> links = new ArrayList<>();

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            Set<Link> foundLinks = user.getLinks();

            if (foundLinks != null && foundLinks.size() > 0) {
                links.addAll(user.getLinks());
            }
        }

        return links;
    }


    public ResponseEntity<HttpHeaders> redirectToUrl(String alias) {
        Optional<Link> link = getUsersLink(alias);
        HttpHeaders headers = new HttpHeaders();

        if (link.isEmpty()) {
            // Searches for link in global user with id 2
            link = linkRepository.findLinkByUserIdAndAlias(2, alias);
        }

        if (link.isPresent()) {
            StringBuilder url = new StringBuilder(link.get().getFullLink());

            if (!url.toString().startsWith("http")) {
                url.insert(0, "https://");
            }

            headers.add("Location", url.toString());

            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        }

        String frontendDomain = System.getenv("FRONTEND_DOMAIN") == null ? "localhost:4200" : System.getenv("FRONTEND_DOMAIN");
        headers.add("Location", "http://" + frontendDomain + "/app-not-found");

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }


    @Transactional
    public void addLink(Link link) {
        Optional<User> user = getUserFromAuthContext();

        if (user.isPresent() && getUsersLink(link.getAlias()).isEmpty()) {
            link.setUser(user.get());
            linkRepository.save(link);
            return;
        }

        throw new LinkAlreadyExistException(link.getAlias());
    }


    @Transactional
    public void addLinkView(String alias) {
        if (getUsersLink(alias).isPresent()) {
            List<Link> links = linkRepository.findLinksByAlias(alias);
            links.forEach(link -> link.setViews(link.getViews() + 1));

            return;
        }

        throw new LinkNotFoundException(alias);
    }

    @Transactional
    public void updateLink(Link link, String alias) {
        Optional<User> user = getUserFromAuthContext();

        if (user.isPresent() && getUsersLink(alias).isPresent()) {
            if (alias.equals(link.getAlias())) {
                Link foundedLink = getUsersLink(alias).get();
                foundedLink.setFullLink(link.getFullLink());
                linkRepository.update(foundedLink.getId(), foundedLink.getAlias(), foundedLink.getFullLink(), foundedLink.getViews());
                return;
            } else {
                if (getUsersLink(link.getAlias()).isEmpty()) {
                    linkRepository.delete(getUsersLink(alias).get());
                    link.setUser(user.get());
                    linkRepository.save(link);
                    return;
                }

                throw new LinkAlreadyExistException(link.getAlias());
            }
        }

        throw new LinkNotFoundException(link.getAlias());
    }


    @Transactional
    public void removeLink(String alias) {
        Optional<Link> link = getUsersLink(alias);

        if (link.isPresent()) {
            linkRepository.delete(link.get());
            return;
        }

        throw new LinkNotFoundException(alias);
    }


    @Transactional
    public void removeAllLinks() {
        Optional<User> user = getUserFromAuthContext();

        if (user.isPresent() && !user.get().getEmail().equals("anonymousUser")) {
            linkRepository.deleteAllByUserId(user.get().getId());
        }
    }

    private Optional<User> getUserFromAuthContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            if (userRepository.findByEmail(authentication.getName()).isPresent()) {
                return userRepository.findByEmail(authentication.getName());
            }

            LOGGER.error("User with email: {} was not found", authentication.getName());
            throw new UsernameNotFoundException(String.format("User with email: %s was not found", authentication.getName()));
        }

        return Optional.empty();
    }

    private boolean isUsersLink(Optional<User> user, Optional<Link> link) {
        return link.isPresent()
                && user.isPresent()
                && Objects.equals(link.get().getUser().getId(), user.get().getId());
    }

    private Optional<Link> getUsersLink(String alias) {
        Optional<User> user = getUserFromAuthContext();

        if (user.isPresent() && linkRepository.findLinkByUserIdAndAlias(user.get().getId(), alias).isPresent()) {
            return linkRepository.findLinkByUserIdAndAlias(user.get().getId(), alias);
        }

        return Optional.empty();
    }
}
