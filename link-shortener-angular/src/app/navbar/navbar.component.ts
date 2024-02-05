
import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';
import { ToastService } from '../toast.service';
import Swal from 'sweetalert2';


@Component({
    selector: 'app-navbar',
    templateUrl: './navbar.component.html',
    styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {
    isDarkMode: boolean = true;

    constructor(private router: Router, private authService: AuthService, public toastService: ToastService) { }

    toggleDarkMode() {
        this.isDarkMode = !this.isDarkMode;
        document.body.classList.toggle('dark-mode', this.isDarkMode);
    }

    getDarkModeIcon(): string {
        return this.isDarkMode ? 'fa-solid fa-moon' : 'fa-regular fa-sun';
    }

    getToken(): string {
        return this.authService.getToken();
    }

    logOut() {
        Swal.fire({
            title: 'Are you sure?',
            text: 'Do you want to log out?',
            icon: 'question',
            showCancelButton: true,
            confirmButtonText: 'Yes, log out',
            cancelButtonText: 'No, cancel'
        }).then((result) => {
            if (result.isConfirmed) {
                this.authService.logOut();
                this.toastService.addSuccess("Log Out", "Successfully logged out.");
                this.router.navigate(['/app-shortener']);
            }
        });
    }
}
