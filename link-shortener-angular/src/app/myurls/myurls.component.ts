import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';
import { LinkService } from '../link.service';
import { ToastService } from '../toast.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-myurls',
  templateUrl: './myurls.component.html',
  styleUrls: ['./myurls.component.css']
})
export class MyurlsComponent {
  toasts = [];
  currentLinks = [];
  links = [];
  page = 1;
  pageSize = 4;
  collectionSize = this.links.length;

  constructor(private linkService: LinkService, public toastService: ToastService, private authService: AuthService, private router: Router) {
    if (authService.getToken() != null) {
      this.getLinks();
    } else {
      toastService.addError("My usrls", "Log in to save your links.");
      this.router.navigate(['/app-shortener']);
    }
  }

  getLinks() {
    this.linkService.getLinks().subscribe({
      next: (response: any) => {
        this.links = response.body;
        this.updateLinks();
      },
      error: (error) => { }
    });
  }

  deleteAllLinks() {
 
    Swal.fire({
      title: 'Are you sure?',
      text: 'This action will delete all links',
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Yes, delete all',
      cancelButtonText: 'No, cancel'
    }).then((result) => {
      if (result.isConfirmed) {
       
        this.linkService.deleteAllLinks().subscribe({
          next: (response: any) => {
            this.toastService.addSuccess('Deleting all links', 'Links were successfully deleted');
            this.refreshLinks();
          },
          error: (error: any) => {
            this.toastService.addError('Deleting all links', 'Something went wrong');
          }
        });
      }
    });
  }

  refreshLinks() {
    this.getLinks();
    this.updateLinks();
  }

  updateLinks() {
    this.currentLinks = this.links.map((link, i) => ({ id: i + 1, ...link })).slice(
      (this.page - 1) * this.pageSize,
      (this.page - 1) * this.pageSize + this.pageSize,
    );
    this.collectionSize = this.links.length;
  }

  redirectToShortener() {
    this.router.navigate(['/app-shortener'])
  }
}
