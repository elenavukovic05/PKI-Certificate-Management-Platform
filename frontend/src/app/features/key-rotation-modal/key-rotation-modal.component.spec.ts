import { ComponentFixture, TestBed } from '@angular/core/testing';

import { KeyRotationModalComponent } from './key-rotation-modal.component';

describe('KeyRotationModalComponent', () => {
  let component: KeyRotationModalComponent;
  let fixture: ComponentFixture<KeyRotationModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [KeyRotationModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(KeyRotationModalComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
