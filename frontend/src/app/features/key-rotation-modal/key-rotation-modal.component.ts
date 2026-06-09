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

  canRotate = false;
  daysUntilRotation = 0;
  newKey: string | null = null;
  confirmed = false;
  copied = false;
  rotating = false;
  errorMessage = '';
  loading = true;

  constructor(private http: HttpClient, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.http.get<any>(`${environment.apiHost}/admin/rotation-status`).subscribe({
      next: (data) => {
        this.canRotate = data.canRotate;
        this.daysUntilRotation = data.daysUntilNextRotation;
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

  rotate(): void {
    console.log('rotate() called');
    this.rotating = true;
    this.errorMessage = '';
    console.log('sending request to:', `${environment.apiHost}/admin/rotate-master-key`);
    this.http.post(
      `${environment.apiHost}/admin/rotate-master-key`,
      {},
      { responseType: 'text' }
    ).subscribe({
      next: (key: string) => {
        console.log('success:', key);
        this.newKey = key;
        this.rotating = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.log('error:', err);
        this.errorMessage = err.error || 'Rotation failed.';
        this.rotating = false;
        this.cdr.detectChanges();
      }
    });
  }

  copyKey(): void {
    if (!this.newKey) return;
    navigator.clipboard.writeText(this.newKey).then(() => {
      this.copied = true;
      setTimeout(() => this.copied = false, 3000);
    });
  }

  close(): void {
    this.closed.emit();
  }
}
