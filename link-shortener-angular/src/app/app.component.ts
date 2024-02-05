import { Component } from '@angular/core';
import { ToastService } from './toast.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'Links shortener';

  constructor(public toastService: ToastService) {
    document.body.classList.add('dark-mode');
  }
}
