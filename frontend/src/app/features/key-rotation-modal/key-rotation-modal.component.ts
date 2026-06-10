import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef, Component, EventEmitter, OnInit, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { environment } from '../../../env/environment';

@Component({
  selector: 'app-key-rotation-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './key-rotation-modal.component.html',
  styleUrl: './key-rotation-modal.component.css',
})
export class KeyRotationModalComponent implements OnInit{
  @Output() closed = new EventEmitter<void>();

  successMessage = '';
  errorMessage = '';
  loading = true;
  rotating = false;
  canRotate = false;
  daysUntilNext = 0;

  constructor(private http: HttpClient, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.loadStatus();
  }

  loadStatus(): void {
    this.loading = true;
    this.http.get<any>(`${environment.apiHost}/admin/rotation-status`).subscribe({
      next: (data) => {
        this.canRotate = data.canRotate;
        this.daysUntilNext = data.daysUntilNextRotation;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Failed to load rotation status.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  rotate(force: boolean = false): void {
    this.rotating = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.http.post(`${environment.apiHost}/admin/rotate-master-key${force ? '?force=true' : ''}`, {}, {
      responseType: 'text'}).subscribe({
      next: (data: string) => {
        this.rotating = false;
        this.successMessage = data;
        this.canRotate = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.rotating = false;
        this.errorMessage = err.error || 'Rotation failed.';
        this.cdr.detectChanges();
      }
    });
  }

  close(): void {
    this.closed.emit();
  }
}
