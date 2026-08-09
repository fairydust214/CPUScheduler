import { Component, OnInit, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Resource } from './resource';
import { ResourceService } from './resource.service';
import { HttpErrorResponse } from '@angular/common/http';


@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  public myResources = signal<Resource[]>([]);

  constructor(private processService: ResourceService){}

  ngOnInit(): void { // when component is initialised
    this.getResources();
  }
  public getResources(): void{
    this.processService.getAllResources().subscribe({
      next: (response: Resource[]) => {
        this.myResources.set(response);
        console.log(this.myResources);
      },
      error: (error: HttpErrorResponse) => {
        alert(error.message);
      }
    });
  }
}
