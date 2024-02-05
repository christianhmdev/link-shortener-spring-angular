import { Component, Input } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { environment } from 'src/environments/environment.development';
import { LinkService } from '../link.service';
import { ToastService } from '../toast.service';
import Swal from 'sweetalert2';


@Component({
  selector: 'app-myurl',
  templateUrl: './myurl.component.html',
  styleUrls: ['./myurl.component.css']
})
export class MyurlComponent {
  @Input() link;
  @Input("currentLinks") links: any;
  updateResult: string;
  environment;


  constructor(private linkService: LinkService, private modalService: NgbModal, public toastService: ToastService) {
    this.environment = environment;
  }

  copyToClipboard(link: any): void {
    this.linkService.copyToClipboard(link);


    const message = `Link copied: ${environment.domain}l/${link.alias}`;
    this.toastService.addSuccess('Link Copied', message);
  }
  deleteLink(link) {
    Swal.fire({
      title: 'Are you sure?',
      text: 'This action will delete the link',
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Yes, delete it',
      cancelButtonText: 'No, cancel'
    }).then((result) => {
      if (result.isConfirmed) {
        this.linkService.deleteLink(link).subscribe({
          next: (response: any) => {

            this.toastService.addSuccess("Deleting link", "Link was successfully deleted.");
          },
          error: (error: any) => {
            if (error.status == "404") {
              Swal.fire('Error', 'Link with this alias was not found!', 'error');
              this.toastService.addError("Deleting link", "Link with this alias was not found.");
            } else {
              Swal.fire('Error', 'Something went wrong', 'error');
              this.toastService.addError("Deleting link", "Something went wrong.");
            }
          },
          complete: () => {
            this.links.splice(this.links.indexOf(link), 1);
          }
        });
      }
    });
  }


  getFavicon(link: any): string {
    return "https://s2.googleusercontent.com/s2/favicons?domain_url=" + link.fullLink;
  }

  openModal(content) {
    this.modalService.open(content, { ariaLabelledBy: 'modal-basic-title' }).result.then(
      (result) => {
        this.updateResult = result;
      },
      (reason) => {
      },
    );
  }

  updateLink(link) {
    let newLink = link.value;

    if (newLink.alias == "") {
      newLink.alias = this.link.alias;
    } else if (newLink.fullLink == "") {
      newLink.fullLink = this.link.fullLink;
    }

    this.linkService.updateLink(newLink, this.link.alias).subscribe({
      next: (response: any) => {
        this.toastService.addSuccess("Updating link", "Link was successfully updated");
      },
      error: (error: any) => {
        if (error.status == "409") {
          this.toastService.addError("Updating link", "Link with this alias already exist!");
        } else {
          this.toastService.addError("Updating link", "Something went wrong");
        }
      }
    });

    link = newLink;
  }
}
