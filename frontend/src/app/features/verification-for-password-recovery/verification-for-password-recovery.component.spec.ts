import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VerificationForPasswordRecoveryComponent } from './verification-for-password-recovery.component';

describe('VerificationForPasswordRecoveryComponent', () => {
  let component: VerificationForPasswordRecoveryComponent;
  let fixture: ComponentFixture<VerificationForPasswordRecoveryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VerificationForPasswordRecoveryComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VerificationForPasswordRecoveryComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
